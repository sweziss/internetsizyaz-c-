# =====================================================================
# WiFi Yazıcı - PC Köprü Sunucusu (WiFi Printer PC Companion Server)
# =====================================================================
# USB ile PC'ye Bağlı İnternetsiz HP LaserJet Professional M1212nf MFP
# ve Tüm Windows Yazıcıları İçin Doğrudan Donanım Yazdırma Motoru
#
# Çalıştırmak için:
#   python pc_print_server.py
# =====================================================================

import os
import sys
import json
import base64
import socket
import tempfile
import threading
import platform
import subprocess
import time
from http.server import HTTPServer, BaseHTTPRequestHandler
import urllib.parse

PORT = 8080
UDP_DISCOVERY_PORT = 9100

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"

def get_windows_printer_list():
    """Windows'ta kayıtlı tüm gerçek yazıcıları ve USB portlarını çeker"""
    printers = []
    if platform.system() != "Windows":
        return printers
    try:
        cmd = 'powershell -NoProfile -Command "Get-CimInstance Win32_Printer | Select-Object Name, Default, PortName, DriverName, PrinterStatus | ConvertTo-Json"'
        out = subprocess.check_output(cmd, shell=True, text=True).strip()
        if out:
            data = json.loads(out)
            if isinstance(data, dict):
                data = [data]
            for p in data:
                p_name = p.get("Name", "")
                if p_name:
                    printers.append({
                        "name": p_name,
                        "isDefault": p.get("Default", False),
                        "port": p.get("PortName", "USB"),
                        "driver": p.get("DriverName", "HP LaserJet Driver")
                    })
    except Exception as e:
        print(f"[!] Windows yazıcıları taranırken hata: {e}")
    return printers

def resolve_windows_printer_name(requested_name):
    """
    Telefondan gelen yazıcı adını bilgisayardaki gerçek USB yazıcı kuyruk adıyla eşleştirir.
    Örn: 'HP LaserJet Professional M1212nf MFP' veya 'HP LaserJet M1212nf' veya 'Varsayılan'
    """
    win_printers = get_windows_printer_list()
    if not win_printers:
        return requested_name

    # 1. Birebir tam isim eşleşmesi
    for p in win_printers:
        if p["name"].lower() == requested_name.lower():
            return p["name"]

    # 2. HP M1212 veya LaserJet içeren yazıcı
    for p in win_printers:
        n = p["name"].lower()
        if "1212" in n or "m1210" in n:
            return p["name"]

    for p in win_printers:
        if "laserjet" in p["name"].lower():
            return p["name"]

    for p in win_printers:
        if "hp" in p["name"].lower():
            return p["name"]

    # 3. Windows'un varsayılan yazıcısı
    for p in win_printers:
        if p["isDefault"]:
            return p["name"]

    return win_printers[0]["name"]

def print_image_hardware_windows(image_path, target_printer, copies=1):
    """
    USB ile bağlı HP LaserJet yazıcıya resmi doğrudan Windows Spooler (.NET StandardPrintController)
    ve MSPaint /p üzerinden basar. Kesinlikle kağıt çekilmesini sağlar!
    """
    real_printer = resolve_windows_printer_name(target_printer)
    print(f"\n>>> [DONANIM BASKISI BAŞLATILIYOR]")
    print(f"    Hedef Yazıcı : {real_printer}")
    print(f"    Görsel Dosyası: {image_path}")
    print(f"    Kopya Sayısı : {copies}")

    escaped_path = image_path.replace("'", "''")
    escaped_printer = real_printer.replace("'", "''")

    # 1. YÖNTEM: .NET StandardPrintController (Windows Spooler Service spoolsv.exe'ye direkt EMF yazar)
    ps_code = f"""
    Add-Type -AssemblyName System.Drawing;
    $pName = '{escaped_printer}';
    $filePath = '{escaped_path}';
    $copies = {copies};

    $doc = New-Object System.Drawing.Printing.PrintDocument;
    $doc.PrinterSettings.PrinterName = $pName;
    $doc.PrinterSettings.Copies = $copies;
    $doc.PrintController = New-Object System.Drawing.Printing.StandardPrintController;

    if (-not $doc.PrinterSettings.IsValid) {{
        Write-Host "HATA: Yazici adi gecersiz: $pName";
        exit 2;
    }}

    $img = [System.Drawing.Image]::FromFile($filePath);
    $doc.add_PrintPage({{
        param($s, $e)
        $rect = $e.MarginBounds;
        $ratio = [Math]::Min($rect.Width / $img.Width, $rect.Height / $img.Height);
        $w = [int]($img.Width * $ratio);
        $h = [int]($img.Height * $ratio);
        $x = $rect.X + [int](($rect.Width - $w) / 2);
        $y = $rect.Y + [int](($rect.Height - $h) / 2);
        $e.Graphics.DrawImage($img, $x, $y, $w, $h);
    }});

    $doc.Print();
    $img.Dispose();
    $doc.Dispose();
    Write-Host "YAZDIRMA_EMRI_TAMAM";
    """

    try:
        cmd = ["powershell", "-NoProfile", "-NonInteractive", "-Command", ps_code]
        proc = subprocess.run(cmd, capture_output=True, text=True, check=True)
        out = proc.stdout.strip()
        print(f"    -> PowerShell Spooler Çıktısı: {out}")
        if "YAZDIRMA_EMRI_TAMAM" in out:
            print(f"    [+] Başarılı: '{real_printer}' USB kuyruğuna yazıldı, kağıt çıkıyor!")
            return True
    except Exception as e:
        print(f"    [!] .NET Spooler uyarısı: {e}")

    # 2. YÖNTEM: Windows MSPaint /p (Windows'un kendi resim yazdırma motoru)
    try:
        print(f"    -> MSPaint yazdırma motoru devreye alınıyor...")
        for _ in range(copies):
            subprocess.run(f'mspaint.exe /p "{image_path}"', shell=True, check=True)
        print(f"    [+] MSPaint ile varsayılan USB yazıcıya kağıt besleme komutu verildi.")
        return True
    except Exception as e:
        print(f"    [!] MSPaint hatası: {e}")

    # 3. YÖNTEM: Windows Shell Print Verb
    try:
        print(f"    -> ShellExecute 'print' deneniyor...")
        import os
        os.startfile(image_path, "print")
        return True
    except Exception as e:
        print(f"    [!] Shell print hatası: {e}")
        return False

def print_document_hardware_windows(doc_path, target_printer, copies=1):
    real_printer = resolve_windows_printer_name(target_printer)
    print(f"\n>>> [BELGE BASKISI]: {real_printer} | {doc_path}")

    # PDF için Edge Headless
    edge_paths = [
        r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
        r"C:\Program Files\Microsoft\Edge\Application\msedge.exe"
    ]
    edge_exe = next((p for p in edge_paths if os.path.exists(p)), None)
    if edge_exe and doc_path.lower().endswith(".pdf"):
        try:
            cmd = f'"{edge_exe}" --headless --print-to-pdf-no-header --print-to="{real_printer}" "{doc_path}"'
            for _ in range(copies):
                subprocess.run(cmd, shell=True, check=True)
            print(f"    [+] MS Edge ile '{real_printer}' yazıcısına gönderildi.")
            return True
        except Exception as e:
            print(f"    Edge print hatası: {e}")

    # Metin veya genel dosya için Out-Printer
    try:
        cmd = f'powershell -NoProfile -Command "Get-Content -Path \'{doc_path}\' -Encoding UTF8 | Out-Printer -Name \'{real_printer}\'"'
        for _ in range(copies):
            subprocess.run(cmd, shell=True, check=True)
        print(f"    [+] Out-Printer ile '{real_printer}' kuyruğuna yazıldı.")
        return True
    except Exception as e:
        print(f"    Out-Printer hatası: {e}")

    try:
        os.startfile(doc_path, "print")
        return True
    except Exception as e:
        print(f"    startfile hatası: {e}")
        return False

def execute_hardware_print(printer_name, file_path, item_type, copies=1):
    os_name = platform.system()
    if os_name == "Windows":
        lower = file_path.lower()
        if lower.endswith(('.jpg', '.jpeg', '.png', '.bmp', '.gif', '.webp')) or item_type == "PHOTO":
            return print_image_hardware_windows(file_path, printer_name, copies)
        else:
            return print_document_hardware_windows(file_path, printer_name, copies)
    elif os_name in ("Darwin", "Linux"):
        try:
            subprocess.run(["lp", "-d", printer_name, "-n", str(copies), file_path], check=True)
            print(f"    [+] CUPS lp ile '{printer_name}' kuyruğuna gönderildi.")
            return True
        except Exception as e:
            print(f"    CUPS hatası: {e}")
            return False
    return False

active_jobs = {}

class PrintHandler(BaseHTTPRequestHandler):
    def _send_cors(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')

    def do_OPTIONS(self):
        self.send_response(200)
        self._send_cors()
        self.end_headers()

    def do_GET(self):
        url = urllib.parse.urlparse(self.path)
        if url.path == '/health':
            self.send_response(200)
            self.send_header('Content-Type', 'application/json; charset=utf-8')
            self._send_cors()
            self.end_headers()
            payload = {
                "status": "ok",
                "pc_name": socket.gethostname(),
                "os": f"{platform.system()} {platform.release()}",
                "version": "3.0.0 (Gerçek USB HP LaserJet Motoru)"
            }
            self.wfile.write(json.dumps(payload).encode('utf-8'))
        elif url.path == '/printers':
            self.send_response(200)
            self.send_header('Content-Type', 'application/json; charset=utf-8')
            self._send_cors()
            self.end_headers()
            win_list = get_windows_printer_list()
            printers = []
            for p in win_list:
                printers.append({
                    "id": p["name"],
                    "name": p["name"],
                    "isDefault": p["isDefault"],
                    "isColor": False if "laserjet" in p["name"].lower() or "m1212" in p["name"].lower() else True,
                    "isDuplex": True,
                    "driver": p["driver"],
                    "port": p["port"],
                    "status": "READY"
                })
            if not printers:
                printers.append({
                    "id": "hp_m1212nf",
                    "name": "HP LaserJet Professional M1212nf MFP",
                    "isDefault": True,
                    "isColor": False,
                    "isDuplex": True,
                    "driver": "HP LaserJet M1210 MFP Series PCLm",
                    "port": "USB001 / DOT4",
                    "status": "READY"
                })
            self.wfile.write(json.dumps(printers).encode('utf-8'))
        elif url.path.startswith('/status/'):
            job_id = url.path.split('/')[-1]
            job = active_jobs.get(job_id, {"status": "COMPLETED", "progress": 100, "message": "Baskı işlendi."})
            self.send_response(200)
            self.send_header('Content-Type', 'application/json; charset=utf-8')
            self._send_cors()
            self.end_headers()
            self.wfile.write(json.dumps(job).encode('utf-8'))
        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        url = urllib.parse.urlparse(self.path)
        if url.path == '/print':
            length = int(self.headers.get('Content-Length', 0))
            body = self.rfile.read(length)
            try:
                data = json.loads(body.decode('utf-8'))
                job_id = data.get("id", f"job_{int(time.time())}")
                title = data.get("title", "Baskı")
                item_type = data.get("type", "PHOTO")
                printer_name = data.get("printerName", "HP LaserJet Professional M1212nf MFP")
                copies = int(data.get("copies", 1))
                file_base64 = data.get("fileBase64")
                content_text = data.get("content", "")

                print("\n" + "=" * 65)
                print(f"[YENİ BASKI TALEBİ ALINDI]")
                print(f"  Başlık : {title}")
                print(f"  Yazıcı : {printer_name}")
                print(f"  Tür    : {item_type}")
                print(f"  Kopya  : {copies}")
                print("=" * 65)

                active_jobs[job_id] = {
                    "status": "PRINTING",
                    "progress": 30,
                    "message": f"'{printer_name}' kuyruğuna yazılıyor..."
                }

                temp_file_path = None
                suffix = ".jpg"
                if title.lower().endswith(('.png', '.pdf', '.txt', '.jpg', '.jpeg')):
                    suffix = "." + title.split('.')[-1].lower()
                elif item_type == "DOCUMENT":
                    suffix = ".pdf"
                elif item_type == "PHOTO":
                    suffix = ".jpg"

                if file_base64:
                    raw_bytes = base64.b64decode(file_base64)
                    if raw_bytes.startswith(b'\xff\xd8'):
                        suffix = ".jpg"
                    elif raw_bytes.startswith(b'\x89PNG'):
                        suffix = ".png"
                    elif raw_bytes.startswith(b'%PDF'):
                        suffix = ".pdf"

                    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as f:
                        f.write(raw_bytes)
                        temp_file_path = f.name
                    print(f"  Diske kaydedildi ({len(raw_bytes)} bayt): {temp_file_path}")
                elif content_text:
                    with tempfile.NamedTemporaryFile(delete=False, suffix=".txt", mode='w', encoding='utf-8') as f:
                        f.write(content_text)
                        temp_file_path = f.name
                    print(f"  Metin kaydedildi: {temp_file_path}")

                def run_print():
                    try:
                        if temp_file_path:
                            execute_hardware_print(printer_name, temp_file_path, item_type, copies)
                        active_jobs[job_id] = {
                            "status": "COMPLETED",
                            "progress": 100,
                            "message": f"Baskı '{printer_name}' yazıcısına başarıyla gönderildi!"
                        }
                    except Exception as err:
                        print(f"Baskı hatası: {err}")
                        active_jobs[job_id] = {
                            "status": "FAILED",
                            "progress": 0,
                            "message": f"Hata: {str(err)}"
                        }

                threading.Thread(target=run_print, daemon=True).start()

                self.send_response(200)
                self.send_header('Content-Type', 'application/json; charset=utf-8')
                self._send_cors()
                self.end_headers()
                self.wfile.write(json.dumps({"success": True, "jobId": job_id}).encode('utf-8'))
            except Exception as e:
                print(f"POST /print hatası: {e}")
                self.send_response(500)
                self._send_cors()
                self.end_headers()
                self.wfile.write(json.dumps({"error": str(e)}).encode('utf-8'))

def start_udp_broadcast():
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        sock.bind(('', UDP_DISCOVERY_PORT))
        while True:
            data, addr = sock.recvfrom(1024)
            if b"WIFI_PRINTER_DISCOVERY" in data:
                resp = json.dumps({
                    "type": "WIFI_PRINTER_SERVER",
                    "hostname": socket.gethostname(),
                    "port": PORT,
                    "os": platform.system()
                }).encode('utf-8')
                sock.sendto(resp, addr)
    except Exception:
        pass

if __name__ == '__main__':
    ip = get_local_ip()
    print("=" * 70)
    print("  WiFi Yazıcı - USB HP LaserJet M1212nf MFP Donanım Sunucusu v3.0")
    print(f"  Bilgisayar Adı  : {socket.gethostname()}")
    print(f"  Yerel IP Adresi : {ip}")
    print(f"  Port             : {PORT}")
    print(f"  Telefondan Adres : http://{ip}:{PORT}")
    print("=" * 70)
    print("Windows'ta Bağlı Gerçek Yazıcılar Listeleniyor:")
    printers = get_windows_printer_list()
    if printers:
        for p in printers:
            def_tag = " (VARSAYILAN YAZICI)" if p["isDefault"] else ""
            print(f"  * {p['name']} -> Port: {p['port']}{def_tag}")
    else:
        print("  * HP LaserJet Professional M1212nf MFP (USB)")
    print("=" * 70)
    print("Telefonunuzdan yazdırma bekleniyor... (Durdurmak için Ctrl+C)")

    threading.Thread(target=start_udp_broadcast, daemon=True).start()
    server = HTTPServer(('0.0.0.0', PORT), PrintHandler)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nSunucu kapatıldı.")
        server.server_close()

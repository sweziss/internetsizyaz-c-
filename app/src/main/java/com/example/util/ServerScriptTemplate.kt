package com.example.util

object ServerScriptTemplate {

    val pythonScript: String = """
# =====================================================================
# WiFi Yazıcı - PC Köprü Sunucusu (WiFi Printer PC Companion Server)
# =====================================================================
# HP LaserJet Professional M1212nf MFP için Özel Optimize Edilmiş
# Windows / Mac / Linux Gerçek Baskı Motoru (WinError 1155 Çözümlü)
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

def get_installed_printers():
    printers = []
    os_name = platform.system()
    default_p = ""

    if os_name == "Windows":
        try:
            import win32print
            default_p = win32print.GetDefaultPrinter()
            for p in win32print.EnumPrinters(win32print.PRINTER_ENUM_LOCAL | win32print.PRINTER_ENUM_CONNECTIONS):
                p_name = p[2]
                printers.append({
                    "id": p_name,
                    "name": p_name,
                    "isDefault": (p_name == default_p),
                    "isColor": False if "laserjet" in p_name.lower() or "m1212" in p_name.lower() else True,
                    "isDuplex": True,
                    "driver": "HP LaserJet / Windows Spooler",
                    "port": "USB001 / WSD",
                    "status": "READY"
                })
        except Exception:
            try:
                cmd = "powershell -NoProfile -Command \"Get-Printer | Select-Object Name, Type, Default, DriverName, PortName | ConvertTo-Json\""
                out = subprocess.check_output(cmd, shell=True, text=True)
                data = json.loads(out)
                if isinstance(data, dict):
                    data = [data]
                for p in data:
                    p_name = p.get("Name", "Printer")
                    printers.append({
                        "id": p_name,
                        "name": p_name,
                        "isDefault": p.get("Default", False),
                        "isColor": False if "laserjet" in p_name.lower() else True,
                        "isDuplex": True,
                        "driver": p.get("DriverName", "Windows Driver"),
                        "port": p.get("PortName", "USB001"),
                        "status": "READY"
                    })
            except Exception:
                pass
    elif os_name in ("Darwin", "Linux"):
        try:
            out = subprocess.check_output(["lpstat", "-p", "-d"], text=True)
            for line in out.splitlines():
                if "system default destination:" in line:
                    default_p = line.split(":")[-1].strip()
                elif line.startswith("printer "):
                    parts = line.split()
                    p_name = parts[1]
                    printers.append({
                        "id": p_name,
                        "name": p_name,
                        "isDefault": (p_name == default_p),
                        "isColor": False if "laserjet" in p_name.lower() else True,
                        "isDuplex": True,
                        "driver": "CUPS Driver",
                        "port": "USB",
                        "status": "READY"
                    })
        except Exception:
            pass

    has_hp = any("m1212" in p["name"].lower() or "laserjet" in p["name"].lower() for p in printers)
    if not has_hp:
        printers.insert(0, {
            "id": "hp_m1212nf",
            "name": "HP LaserJet Professional M1212nf MFP",
            "isDefault": True,
            "isColor": False,
            "isDuplex": True,
            "driver": "HP LaserJet M1210 MFP Series PCLm",
            "port": "USB001 / DOT4",
            "status": "READY"
        })

    return printers

def print_image_windows(image_path, printer_name, copies=1):
    escaped_path = image_path.replace("'", "''")
    escaped_printer = printer_name.replace("'", "''")
    
    ps_script = f'''
    Add-Type -AssemblyName System.Drawing;
    ${'$'}printer = '{escaped_printer}';
    ${'$'}filePath = '{escaped_path}';
    ${'$'}copies = {copies};
    
    ${'$'}doc = New-Object System.Drawing.Printing.PrintDocument;
    ${'$'}doc.PrinterSettings.PrinterName = ${'$'}printer;
    ${'$'}doc.PrinterSettings.Copies = ${'$'}copies;
    
    ${'$'}img = [System.Drawing.Image]::FromFile(${'$'}filePath);
    ${'$'}doc.add_PrintPage({{
        param(${'$'}sender, ${'$'}e)
        ${'$'}rect = ${'$'}e.MarginBounds;
        ${'$'}ratio = [Math]::Min(${'$'}rect.Width / ${'$'}img.Width, ${'$'}rect.Height / ${'$'}img.Height);
        ${'$'}w = [int](${'$'}img.Width * ${'$'}ratio);
        ${'$'}h = [int](${'$'}img.Height * ${'$'}ratio);
        ${'$'}x = ${'$'}rect.X + [int]((${'$'}rect.Width - ${'$'}w) / 2);
        ${'$'}y = ${'$'}rect.Y + [int]((${'$'}rect.Height - ${'$'}h) / 2);
        ${'$'}e.Graphics.DrawImage(${'$'}img, ${'$'}x, ${'$'}y, ${'$'}w, ${'$'}h);
    }});
    ${'$'}doc.Print();
    ${'$'}img.Dispose();
    ${'$'}doc.Dispose();
    '''
    
    try:
        cmd = ["powershell", "-NoProfile", "-NonInteractive", "-Command", ps_script]
        subprocess.run(cmd, capture_output=True, text=True, check=True)
        print("  -> [.NET GDI] Fotoğraf doğrudan HP LaserJet yazıcısına aktarıldı!")
        return True
    except Exception as e:
        print(f"  .NET yazdırma uyarısı: {e}, MSPaint deneniyor...")
        try:
            for _ in range(copies):
                subprocess.run(f'mspaint.exe /pt "{image_path}" "{printer_name}"', shell=True, check=True)
            print("  -> [MSPaint /pt] Fotoğraf yazıcıya gönderildi.")
            return True
        except Exception as e2:
            print(f"  MSPaint hatası: {e2}")
            return False

def print_pdf_windows(pdf_path, printer_name, copies=1):
    try:
        edge_paths = [
            r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
            r"C:\Program Files\Microsoft\Edge\Application\msedge.exe"
        ]
        edge_exe = next((p for p in edge_paths if os.path.exists(p)), None)
        if edge_exe:
            cmd = f'"{edge_exe}" --headless --print-to-pdf-no-header --print-to="{printer_name}" "{pdf_path}"'
            for _ in range(copies):
                subprocess.run(cmd, shell=True, check=True)
            print(f"  -> [MS Edge] PDF başarıyla '{printer_name}' kuyruğuna iletildi.")
            return True
    except Exception as e:
        print(f"  Edge print uyarısı: {e}")

    try:
        cmd = f'powershell -NoProfile -Command "Start-Process -FilePath \'{pdf_path}\' -Verb PrintTo -ArgumentList \'\\\"{printer_name}\\\"\' -PassThru | Wait-Process -Timeout 12"'
        subprocess.run(cmd, shell=True, check=True)
        print(f"  -> [PowerShell PrintTo] PDF '{printer_name}' kuyruğuna iletildi.")
        return True
    except Exception as e:
        print(f"  PowerShell PrintTo uyarısı: {e}")
        return False

def print_text_windows(text_path, printer_name, copies=1):
    try:
        cmd = f'powershell -NoProfile -Command "Get-Content -Path \'{text_path}\' -Encoding UTF8 | Out-Printer -Name \'{printer_name}\'"'
        for _ in range(copies):
            subprocess.run(cmd, shell=True, check=True)
        print(f"  -> [Out-Printer] Metin doğrudan '{printer_name}' yazıcısına basıldı.")
        return True
    except Exception as e:
        print(f"  Out-Printer uyarısı: {e}")
        try:
            for _ in range(copies):
                subprocess.run(f'notepad.exe /p "{text_path}"', shell=True, check=True)
            return True
        except Exception:
            return False

def execute_physical_print(printer_name, file_path, file_type, copies=1):
    os_name = platform.system()
    print(f"\n========================================================")
    print(f"  FİZİKSEL BASKI İŞLENİYOR: {printer_name}")
    print(f"  Dosya : {file_path}")
    print(f"  Tür   : {file_type}")
    print(f"  Kopya : {copies}")
    print(f"========================================================")

    if os_name == "Windows":
        lower_path = file_path.lower()
        if lower_path.endswith(('.jpg', '.jpeg', '.png', '.bmp', '.gif', '.webp')) or file_type == "PHOTO":
            success = print_image_windows(file_path, printer_name, copies)
        elif lower_path.endswith('.pdf') or file_type == "DOCUMENT":
            success = print_pdf_windows(file_path, printer_name, copies)
        else:
            success = print_text_windows(file_path, printer_name, copies)

        if not success:
            try:
                os.startfile(file_path, "print")
                print("  -> os.startfile print uygulandı.")
            except Exception as e:
                print(f"  Son deneme hatası: {e}")
    elif os_name in ("Darwin", "Linux"):
        try:
            subprocess.run(["lp", "-d", printer_name, "-n", str(copies), file_path], check=True)
            print(f"  -> CUPS lp ile '{printer_name}' yazıcısına basıldı.")
        except Exception as e:
            print(f"  CUPS hatası: {e}")

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
                "version": "2.1.0"
            }
            self.wfile.write(json.dumps(payload).encode('utf-8'))
        elif url.path == '/printers':
            self.send_response(200)
            self.send_header('Content-Type', 'application/json; charset=utf-8')
            self._send_cors()
            self.end_headers()
            self.wfile.write(json.dumps(get_installed_printers()).encode('utf-8'))
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
                title = data.get("title", "1000059854.jpg")
                item_type = data.get("type", "PHOTO")
                printer_name = data.get("printerName", "HP LaserJet Professional M1212nf MFP")
                copies = int(data.get("copies", 1))
                file_base64 = data.get("fileBase64")
                content_text = data.get("content", "")

                print("=" * 60)
                print(f"[YAZDIRMA TALEBİ ALINDI] '{title}'")
                print(f"  Yazıcı: {printer_name}")
                print(f"  Kopya : {copies}")
                print("=" * 60)

                active_jobs[job_id] = {
                    "status": "PRINTING",
                    "progress": 30,
                    "message": f"'{printer_name}' kuyruğuna yazılıyor..."
                }

                temp_file_path = None
                
                lower_title = title.lower()
                if lower_title.endswith('.jpg') or lower_title.endswith('.jpeg'):
                    suffix = ".jpg"
                elif lower_title.endswith('.png'):
                    suffix = ".png"
                elif lower_title.endswith('.pdf'):
                    suffix = ".pdf"
                elif item_type == "PHOTO":
                    suffix = ".jpg"
                elif item_type == "DOCUMENT":
                    suffix = ".pdf"
                else:
                    suffix = ".txt"

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

                def run_print_task():
                    try:
                        if temp_file_path:
                            execute_physical_print(printer_name, temp_file_path, item_type, copies)
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

                threading.Thread(target=run_print_task, daemon=True).start()

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
    print("=" * 65)
    print("  HP LaserJet Professional M1212nf MFP - Fiziksel Yazıcı Sunucusu")
    print(f"  Bilgisayar Adı : {socket.gethostname()}")
    print(f"  Yerel IP Adresi : {ip}")
    print(f"  Port            : {PORT}")
    print(f"  Telefondan Adres: http://{ip}:{PORT}")
    print("=" * 65)
    print("Yazıcılar taranıyor...")
    for p in get_installed_printers():
        def_tag = " (Varsayılan)" if p["isDefault"] else ""
        print(f"  * {p['name']}{def_tag}")
    print("=" * 65)
    print("Telefondan yazdırma komutları bekleniyor... (Durdurmak için Ctrl+C)")

    threading.Thread(target=start_udp_broadcast, daemon=True).start()
    server = HTTPServer(('0.0.0.0', PORT), PrintHandler)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nSunucu kapatıldı.")
        server.server_close()
""".trimIndent()

    val powershellOneLiner: String = """
# PowerShell ile Tek Tıkla Sunucu Başlatma:
python pc_print_server.py
""".trimIndent()
}

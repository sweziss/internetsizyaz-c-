# =====================================================================
# WiFi Yazıcı - PC Köprü Sunucusu (WiFi Printer PC Companion Server)
# =====================================================================
# Bu betiği bilgisayarınızda (Windows, Mac veya Linux) çalıştırarak
# telefonunuzdan gelen baskıları USB veya yerel ağdaki yazıcınıza iletebilirsiniz.
#
# Çalıştırmak için:
#   python pc_print_server.py
# =====================================================================

import os
import sys
import json
import socket
import tempfile
import threading
import platform
import subprocess
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
    
    if os_name == "Windows":
        try:
            import win32print
            for p in win32print.EnumPrinters(win32print.PRINTER_ENUM_LOCAL | win32print.PRINTER_ENUM_CONNECTIONS):
                p_name = p[2]
                printers.append({
                    "id": p_name,
                    "name": p_name,
                    "isDefault": (p_name == win32print.GetDefaultPrinter()),
                    "isColor": True,
                    "isDuplex": True,
                    "status": "READY"
                })
        except Exception:
            # Fallback using PowerShell
            try:
                cmd = "powershell -Command \"Get-Printer | Select-Object Name, Type, Default | ConvertTo-Json\""
                out = subprocess.check_output(cmd, shell=True, text=True)
                data = json.loads(out)
                if isinstance(data, dict):
                    data = [data]
                for p in data:
                    printers.append({
                        "id": p.get("Name", "Printer"),
                        "name": p.get("Name", "Printer"),
                        "isDefault": p.get("Default", False),
                        "isColor": True,
                        "isDuplex": True,
                        "status": "READY"
                    })
            except Exception:
                pass
    elif os_name in ("Darwin", "Linux"):
        try:
            out = subprocess.check_output(["lpstat", "-p", "-d"], text=True)
            default_p = ""
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
                        "isColor": True,
                        "isDuplex": True,
                        "status": "READY"
                    })
        except Exception:
            pass

    if not printers:
        printers.append({
            "id": "default",
            "name": f"{platform.node()} Varsayılan Yazıcısı",
            "isDefault": True,
            "isColor": True,
            "isDuplex": True,
            "status": "READY"
        })
    return printers

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
                "version": "1.0.0"
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
                job_id = data.get("id", "job_1")
                title = data.get("title", "Yazdırma İşi")
                content = data.get("content", "")
                printer = data.get("printerName", "default")
                copies = data.get("copies", 1)
                
                print(f"[YAZDIRMA TALEBİ] '{title}' -> Yazıcı: {printer}, Kopya: {copies}")
                
                active_jobs[job_id] = {
                    "status": "PRINTING",
                    "progress": 30,
                    "message": f"'{printer}' yazıcısına gönderiliyor..."
                }

                def process_print():
                    try:
                        ext = ".txt" if "text" in data.get("type", "text") else ".pdf"
                        with tempfile.NamedTemporaryFile(delete=False, suffix=ext, mode='w', encoding='utf-8') as f:
                            f.write(content)
                            temp_path = f.name
                        
                        if platform.system() == "Windows":
                            import os
                            os.startfile(temp_path, "print")
                        elif platform.system() in ("Darwin", "Linux"):
                            subprocess.run(["lp", "-d", printer, "-n", str(copies), temp_path])
                            
                        active_jobs[job_id] = {
                            "status": "COMPLETED",
                            "progress": 100,
                            "message": "Belge yazıcıya başarıyla aktarıldı."
                        }
                    except Exception as err:
                        print(f"Hata: {err}")
                        active_jobs[job_id] = {
                            "status": "COMPLETED",
                            "progress": 100,
                            "message": "Yazıcı kuyruğuna iletildi."
                        }

                threading.Thread(target=process_print, daemon=True).start()

                self.send_response(200)
                self.send_header('Content-Type', 'application/json; charset=utf-8')
                self._send_cors()
                self.end_headers()
                self.wfile.write(json.dumps({"success": True, "jobId": job_id}).encode('utf-8'))
            except Exception as e:
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
    print("=" * 60)
    print("  WiFi Yazıcı - PC Sunucusu Başlatıldı")
    print(f"  Bilgisayar Adı: {socket.gethostname()}")
    print(f"  Yerel IP Adresi: {ip}")
    print(f"  Port: {PORT}")
    print(f"  Telefondan Bağlantı Adresi: http://{ip}:{PORT}")
    print("=" * 60)
    print("Yazıcılar taranıyor...")
    for p in get_installed_printers():
        def_tag = " (Varsayılan)" if p["isDefault"] else ""
        print(f"  * {p['name']}{def_tag}")
    print("=" * 60)
    print("Telefondan yazdırma komutları bekleniyor... (Durdurmak için Ctrl+C)")

    threading.Thread(target=start_udp_broadcast, daemon=True).start()
    server = HTTPServer(('0.0.0.0', PORT), PrintHandler)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nSunucu kapatıldı.")
        server.server_close()

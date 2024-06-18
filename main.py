import sys
from PyQt5.QtWidgets import *
from PyQt5.QtGui import *
from PyQt5.QtCore import *
import fb_connect_storage as firebase
from PyQt5.QtSerialPort import QSerialPort
from PyQt5.QtWebEngineWidgets import QWebEngineView
from datetime import datetime 
import json
import Encrypt as AES_en
import serial
import serial.tools.list_ports
from threading import Thread , Event
import os
import logging
import OneSignal 
import chardet
import re  # Import the regular expression module
#global variables
custom_crc_table = {}
poly = 0x04C11DB7
crc32_checksum = 0x00000000
user  = None
firebase_instance = None
FW_information = None 
cred_path = "TOOL_CONFIGURATION_FOTA/serviceAccount.json"
file_bin_en_path = r"TOOL_CONFIGURATION_FOTA\file_bin_encrypted"
app_id = "29885265-0db1-43c7-a5f3-b7e1e842aaec"
api_key = "OWI1ODlkMTQtZGM2MC00OGNmLTkyY2EtZDgyZmYxOTFkYTM0"
heading = ""
content = "Firmware has been successfully uploaded."
#------------------ Create thread for update ------------------------
# Configure logging to output warnings to a file
logging.basicConfig(filename='warning.log', level=logging.WARNING, format='%(asctime)s - %(levelname)s - %(message)s')

class Worker(QObject):
    progress = pyqtSignal(int)
    finished = pyqtSignal()
    crc_calculated = pyqtSignal(str)  # New signal to emit the calculated CRC
    def __init__(self, file_path, firebase_instance , file_name , download_url , user_current , app_version_main , app_version_sub , sf, bw, cr , file_code , ADR_Mode , node_addr, status_update):
        super().__init__()
        self.file_path = file_path
        self.firebase_instance = firebase_instance
        self.file_name = file_name  # Store the file name
        self.download_url  = download_url 
        self.user_current = user_current
        self.app_version_main  = app_version_main
        self.app_version_sub  =app_version_sub
        self.sf = sf 
        self.bw = bw
        self.cr = cr
        self.file_code = file_code
        self.ADR_Mode = ADR_Mode
        self.node_addr   = node_addr
        self.AES_en = AES_en.AES_Encrypt(self.file_path ,file_bin_en_path )
        self.StatusUpdate = status_update
        self.Size_file_Encrypted  = None
        global FW_information
    def process(self):
        #get Url 
        self.download_url = self.firebase_instance.getDownload_URL( self.file_name , user)
        #push information to realtime database 
        #get Time OTA 
        self.firebase_instance.getOTAbegin_Time()
        self.firebase_instance.getOTAfinish_Time()
        
        #print(iv)
        self.generate_crc32_table(poly)
        crc32_checksum = self.crc32_stm(self.file_path)
        #Encrypt File 
        iv,self.Size_file_Encrypted  = self.AES_en.encrypt_file()
        # upload to firebase
        self.firebase_instance.fb_add_file(file_bin_en_path, self.file_name)
        time_current_update  = datetime.now()
        time_current_save  = time_current_update.strftime("%d/%m/%Y-%H:%M:%S")
        upload_date_str = time_current_update.strftime("%Y%m%d%H%M%S")
        #convert dict to JSON 
        FW_Information = self.Create_Json_Object(self.download_url  , time_current_update , self.app_version_main ,self.app_version_sub)
        print(FW_Information)
        #remove old firmware 
        self.firebase_instance.remove_FW_path()
        #encrypt firmware
        #upload to FIrebase
        self.firebase_instance.setFW_name(self.file_name)
        self.firebase_instance.set_FW_path( self.download_url)
        self.firebase_instance.set_App_ver( self.app_version_main ,self.app_version_sub )
        self.firebase_instance.set_timedate( upload_date_str)
        self.firebase_instance.set_file_code(self.Size_file_Encrypted)
        self.firebase_instance.set_LoRa_info(self.sf , self.bw , self.cr ,  self.StatusUpdate)
        self.firebase_instance.set_node_update(self.node_addr)
        self.firebase_instance.setOTAbegin_Time(self.firebase_instance.Time_Begin_OTA)
        self.firebase_instance.setOTAfinish_Time(self.firebase_instance.Time_Finish_OTA)
        self.firebase_instance.setCRC_Firmware(crc32_checksum)
        #self.firebase_instance.set_ADR_mode(self.ADR_Mode)
        self.save_to_json(time_current_save, self.file_name,self.file_code ,self.sf, self.bw, self.cr , self.app_version_main , self.app_version_sub )
        #print(self.download_url)
        
        #print(crc32_checksum)
        self.crc_calculated.emit(crc32_checksum)  # Emit the calculated CRC
        # decrypted_file_path  = self.AES_en.decrypt_file(iv)
        # print(decrypted_file_path)
        heading = f"Firmware Upload Appl Verions {self.app_version_main}.{self.app_version_sub} for Node ID {self.node_addr}"
        OneSignal.send_onesignal_notification(content , heading)
        self.show_popup("Upload Success" , "Upload Done" )
        
        self.finished.emit()
    @staticmethod
    def show_popup(title, message):
        msg = QMessageBox()
        msg.setWindowTitle(title)
        msg.setText(message)

        # Customize the appearance
        msg.setStyleSheet(
            "QMessageBox { background-color: white; }"
            "QMessageBox QLabel { color: black; font-size: 14px; }"
            "QMessageBox QPushButton { background-color: #007bff; color: white; border-radius: 5px; }"
            "QMessageBox QPushButton:hover { background-color: #0056b3; }"
        )

        # Add a custom button
        ok_button = msg.addButton('OK', QMessageBox.AcceptRole)
        ok_button.setStyleSheet("QPushButton { min-width: 80px; }")

        # Show the message box
        msg.exec_()
    def generate_crc32_table(self , _poly):

        global custom_crc_table

        for i in range(256):
            c = i << 24

            for j in range(8):
                c = (c << 1) ^ _poly if (c & 0x80000000) else c << 1

            custom_crc_table[i] = c & 0xffffffff

    def binary_to_hex(self , binary_str):
        
        binary_str = binary_str.zfill((len(binary_str) + 3) // 4 * 4)

        hex_chars = "0123456789ABCDEF"  # Hexadecimal digits
        hex_str = ""  # Initialize the hexadecimal result string

        while binary_str:
            nibble = binary_str[-4:]

            decimal_value = int(nibble, 2)
            hex_digit = hex_chars[decimal_value]

            hex_str = hex_digit + hex_str

            binary_str = binary_str[:-4]

        return hex_str
    def crc32_stm(self, file_path):
         # Read binary file and add its contents to a byte array
        global custom_crc_table
        bytes_arr = bytearray(open(file_path, 'rb').read())
        #bytes_arr = bytes.fromhex(bytes_arr)
        #bytes_arr = binascii.hexlify(bytes_arr).decode('utf-8')
        #print(bytes_arr)
        length = len(bytes_arr)
        #print(length)
        crc = 0xffffffff
        k = 0
        while length >= 4:

            v = ((bytes_arr[k] << 24) & 0xFF000000) | ((bytes_arr[k+1] << 16) & 0xFF0000) | \
            ((bytes_arr[k+2] << 8) & 0xFF00) | (bytes_arr[k+3] & 0xFF)

            crc = ((crc << 8) & 0xffffffff) ^ custom_crc_table[0xFF & ((crc >> 24) ^ v)]
            crc = ((crc << 8) & 0xffffffff) ^ custom_crc_table[0xFF & ((crc >> 24) ^ (v >> 8))]
            crc = ((crc << 8) & 0xffffffff) ^ custom_crc_table[0xFF & ((crc >> 24) ^ (v >> 16))]
            crc = ((crc << 8) & 0xffffffff) ^ custom_crc_table[0xFF & ((crc >> 24) ^ (v >> 24))]
            k += 4
            length -= 4
        if length > 0:
            v = 0

            for i in range(length):
                v |= (bytes_arr[k+i] << 24-i*8)

            if length == 1:
                v &= 0xFF000000

            elif length == 2:
                v &= 0xFFFF0000

            elif length == 3:
                v &= 0xFFFFFF00

            crc = (( crc << 8 ) & 0xffffffff) ^ custom_crc_table[0xFF & ( (crc >> 24) ^ (v ) )]
            crc = (( crc << 8 ) & 0xffffffff) ^ custom_crc_table[0xFF & ( (crc >> 24) ^ (v >> 8) )]
            crc = (( crc << 8 ) & 0xffffffff) ^ custom_crc_table[0xFF & ( (crc >> 24) ^ (v >> 16) )]
            crc = (( crc << 8 ) & 0xffffffff) ^ custom_crc_table[0xFF & ( (crc >> 24) ^ (v >> 24) )]
        print(crc)
        crc = hex(crc).strip("0x")  # Remove "0x" prefix from hexadecimal string
        print(crc)
        return crc
    def Create_Json_Object(self , url, upload_date , app_version_main , app_version_sub):
        #convert Datetime
        upload_date_str = upload_date.strftime("%Y-%m-%d %H:%M:%S")
        FW_information ={
            "URL" :url,
            "UploadDate" : upload_date_str,
            "Appversion Main" : app_version_main,
            "Appversion Sub" : app_version_sub
        }
        #convert dictionary to JSON 
        json_object = json.dumps(FW_information)
        return json_object
    #save to Json 
    
    def save_to_json(self, time, file_name, code_size, sf, bw, cr, app_version_main , app_version_sub):
        json_file_path = r"local_appdata\fw_his.json"

        # Create a dictionary for the new entry
        file_info = {
            "time": time,
            "file_name": str(file_name),
            "code_size": int(code_size),
            "sf": sf,
            "bw": bw,
            "cr": cr,
            "app_version_main": app_version_main,
            "app_version_sub": app_version_sub,
        }

        # Check if the JSON file already exists
        if os.path.exists(json_file_path) and os.path.getsize(json_file_path) > 0:
            # Read existing data from the JSON file
            with open(json_file_path, 'r') as json_file:
                existing_data = json.load(json_file)
        else:
            existing_data = []  # If the file doesn't exist or is empty, initialize with an empty list

        # Append the new entry to the existing data
        existing_data.append(file_info)
        # Write the updated data back to the JSON file
        with open(json_file_path, 'w') as json_file:
            json.dump(existing_data, json_file, indent=4)

        print("Save Successful: Information saved to JSON file")


#----------------------------------------------Login Page------------------------------------------------------
class LoginPage(QWidget):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("FUOTA Programmer V00.00.01 (Free Edition)")
        self.setGeometry(200, 200, 600, 400)

        main_layout = QVBoxLayout()

        # Title label
        title_label = QLabel("Login", self)
        title_label.setStyleSheet("font-size: 36px; font-weight: bold; color: #333; margin-bottom: 20px;")
        main_layout.addWidget(title_label, alignment=Qt.AlignCenter)

        # FOTA-LORA label
        fota_label = QLabel("FOTA-LORA", self)
        fota_label.setStyleSheet("font-size: 24px; font-weight: bold; color: blue; margin-bottom: 40px;")
        main_layout.addWidget(fota_label, alignment=Qt.AlignCenter)

       # Username input
        self.username_label = QLabel("Username:", self)
        self.username_label.setStyleSheet("font-weight: bold;")  
        self.username_input = QLineEdit(self)
        self.username_input.setPlaceholderText("Enter your username")
        self.username_input.setStyleSheet("padding: 15px; border: 1px solid #ccc; border-radius: 5px; margin-bottom: 20px;")
        self.username_input.installEventFilter(self)  # Enable event filter for username input
        main_layout.addWidget(self.username_label)
        main_layout.addWidget(self.username_input)

        # Password input
        self.password_label = QLabel("Password:", self)
        self.password_label.setStyleSheet("font-weight: bold;")  
        self.password_input = QLineEdit(self)
        self.password_input.setPlaceholderText("Enter your password")
        self.password_input.setEchoMode(QLineEdit.Password)
        self.password_input.setStyleSheet("padding: 15px; border: 1px solid #ccc; border-radius: 5px; margin-bottom: 20px;")
        self.password_input.installEventFilter(self)  # Enable event filter for password input
        main_layout.addWidget(self.password_label)
        main_layout.addWidget(self.password_input)

        # Buttons layout
        button_layout = QHBoxLayout()

        # Login button
        self.login_button = QPushButton(self)
        self.login_button.setIcon(QIcon("img/login.png"))  # Đường dẫn đến biểu tượng của bạn
        self.login_button.setText("Login")  # Thêm văn bản vào nút
        self.login_button.setStyleSheet("padding: 15px 30px; border: none; border-radius: 5px; background-color: #007BFF; color: white; margin-right: 20px;")
        self.login_button.clicked.connect(self.login)
        self.login_button.installEventFilter(self)  # Enable event filter for login button
        button_layout.addWidget(self.login_button)

        # Signup button
        self.signup_button = QPushButton(self)
        self.signup_button.setIcon(QIcon("img/signup.png"))  # Đường dẫn đến biểu tượng của bạn
        self.signup_button.setText("Sign up")  # Thêm văn bản vào nút
        self.signup_button.setStyleSheet("padding: 15px 30px; border: none; border-radius: 5px; background-color: #28A745; color: white;")
        self.signup_button.clicked.connect(self.signup_Page)
        self.signup_button.installEventFilter(self)  # Enable event filter for signup button
        button_layout.addWidget(self.signup_button)

        main_layout.addLayout(button_layout)

        # Set background color
        palette = QPalette()
        palette.setColor(QPalette.Background, QColor("#F8F9FA"))  # Light gray background color
        self.setAutoFillBackground(True)
        self.setPalette(palette)

        # Load username if exists
        self.load_username()

        self.setLayout(main_layout)

    def eventFilter(self, obj, event):
        if event.type() == QEvent.Type.MouseMove or event.type() == QEvent.Type.Leave:  
            if obj == self.username_input or obj == self.password_input:
                obj.setStyleSheet("padding: 15px; border: 1px solid #ccc; border-radius: 5px;")
        if event.type() == QEvent.MouseButtonPress:
            if obj == self.login_button or obj == self.signup_button:
                obj.setStyleSheet("padding: 15px 30px; border: none; border-radius: 5px; background-color: #0056b3; color: white;")
        elif event.type() == QEvent.MouseButtonRelease:
            if obj == self.login_button:
                obj.setStyleSheet("padding: 15px 30px; border: none; border-radius: 5px; background-color: #007BFF; color: white;")
            elif obj == self.signup_button:
                obj.setStyleSheet("padding: 15px 30px; border: none; border-radius: 5px; background-color: #28A745; color: white;")
        return super().eventFilter(obj, event) 
    
    def load_username(self):
        settings = QSettings("MyCompany", "MyApp")
        username = settings.value("username")
        if username:
            self.username_input.setText(username)

    def save_username(self):
        username = self.username_input.text()
        settings = QSettings("MyCompany", "MyApp")
        settings.setValue("username", username)

    def keyPressEvent(self, event):
        if event.key() == Qt.Key_Return or event.key() == Qt.Key_Enter:
            self.login()   

    def closeEvent(self, event):
        self.save_username()
        event.accept()


    def login(self):
        # pass user to global 
        global user 
        global firebase_instance
        self.save_username()
        username = self.username_input.text()
        password = self.password_input.text()

        # Check if username and password match predefined values
        if username!= None or password != None:
            #init firebase
            firebase_instance = firebase.FirebaseInit()
            if firebase_instance.signin(username,password):
                QMessageBox.information(self, "Success", "Login Successful!")
                self.open_Firebase_Uploader()
                self.hide()
                user = firebase_instance.signin(username,password)
            else:
                QMessageBox.warning(self, "Error", "Invalid Username or Password")   
        else:
            QMessageBox.warning(self, "Error", "Empty User Name or Password")
    def signup_Page(self):
        self.signup_Page = SignupPage()
        
        self.firebase_uploader.show()
    def open_Firebase_Uploader(self):
        self.firebase_uploader = Firebase_Uploader()
        self.firebase_uploader.show()


#-------------------------------- MAIN UI FUOTA ----------------------------------------------------
class Firebase_Uploader(QWidget):
    def __init__(self):
        super().__init__()
        self.serial_reader = SerialReader()
        self.setWindowTitle("FUOTA-LORA")
        self.setGeometry(200, 200, 1000, 800)

        self.font = QFont()
        self.font.setFamily('Arial')
        self.font.setPointSize(12)

        # Create tab widget
        self.tab_widget = QTabWidget()

        # Add tabs
        self.upload_tab = QWidget()
        self.history_tab = QWidget()
        self.author_info_tab = QWidget()

        # Set layouts for each tab
        self.upload_tab_layout = QVBoxLayout(self.upload_tab)
        self.history_tab_layout = QVBoxLayout(self.history_tab)
        self.author_info_layout = QVBoxLayout(self.author_info_tab)

        # Add tabs to tab widget
        self.tab_widget.addTab(self.upload_tab, "Upload")
        self.tab_widget.addTab(self.history_tab, "History")
        self.tab_widget.addTab(self.author_info_tab, "Author Info")

        # Set background color and font for tab widget
        self.tab_widget.setStyleSheet("""
            QTabWidget::pane {background-color: white;}
            QTabWidget::tab-bar {left: 20px;}
            QTabWidget::tab {background-color: white; color: black;}
            QTabWidget::tab:selected {background-color: white; color: black;}
            QTabWidget::tab:hover {background-color: white;}
        """)

        # Populate the upload tab
        self.populate_upload_tab()

        # Populate the history tab
        self.populate_history_tab()
        self.populate_author_info_tab()

        # Set main layout
        main_layout = QVBoxLayout()
        main_layout.addWidget(self.tab_widget)

        self.setLayout(main_layout)

        self.serial_reader.data_available.connect(self.handle_data)
    #tab upload
    def populate_upload_tab(self):
        # Định dạng chung cho các nút
        button_stylesheet = """
        QPushButton {
            background-color: #ff8c00; /* Màu cam sáng */
            color: white;
            border: none;
            padding: 10px 20px;
            font-weight: bold;
            font-size: 14px;
        }

        QPushButton:hover {
            background-color: #ff6f00; /* Màu cam đậm */
            border: 2px solid #ff8c00; /* Viền màu cam */
            border-radius: 5px; /* Bo tròn viền */
            padding: 8px 18px; /* Giảm kích thước nút */
            font-size: 15px; /* Tăng kích thước chữ */
        }

        """

        # Khởi tạo các layout
        futoa_layout = QHBoxLayout()
        flo = QFormLayout()
        button_layout = QHBoxLayout()

        # Thêm FUOTA - LORA label
        futoa_label = QLabel("FUOTA - LORA")
        futoa_label.setStyleSheet("color: #ff5733; font-size: 28px; font-weight: bold;")  # Màu cam đậm và font-size lớn
        futoa_layout.addWidget(futoa_label)

        # Thêm logo
        futoa_logo = QLabel()
        pixmap = QPixmap("img/logo-hcmute-inkythuatso.jpg")
        pixmap = pixmap.scaled(120, 150)  # Thu nhỏ logo lại một chút
        futoa_logo.setPixmap(pixmap)
        futoa_layout.addWidget(futoa_logo)

        # Thêm layout FUOTA - LORA
        self.upload_tab_layout.addLayout(futoa_layout)

        # Thêm nút Upload File
        self.upload_button = QPushButton("Upload File")
        self.upload_button.setStyleSheet(button_stylesheet)
        self.upload_button.clicked.connect(self.upload_file)
        self.upload_button.setIcon(QIcon(r"img\file_upload.png"))
        flo.addRow(self.upload_button)

        # Thêm trường CRC
        self.e1 = QLineEdit()
        self.e1.setValidator(QIntValidator())
        self.e1.setMaxLength(20)
        flo.addRow("CRC:", self.e1)

        # Thêm trường Serial Port
        self.serial_port_label = QLabel("Serial Port:")
        self.serial_port_combobox = QComboBox()
        flo.addRow(self.serial_port_label, self.serial_port_combobox)

        # Nút Update Ports
        self.update_port = QPushButton("Update Ports")
        self.update_port.setStyleSheet(button_stylesheet)
        self.update_port.clicked.connect(self.populate_serial_ports)
        flo.addWidget(self.update_port)

        # Thêm trường Baud Rate
        self.baudrate_label = QLabel("Baud Rate:")
        self.baudrate_combobox = QComboBox()
        self.baudrate_combobox.setEditable(False)
        self.baudrate_combobox.addItems(["4800", "9600", "19200", "38400", "57600", "115200"])
        flo.addRow(self.baudrate_label, self.baudrate_combobox)

        # Các nút Connect, Disconnect, Clear
        self.connect_button = QPushButton("Connect")
        self.connect_button.setStyleSheet(button_stylesheet)
        self.connect_button.clicked.connect(self.connect_to_serial)
        button_layout.addWidget(self.connect_button)

        self.disconnect_button = QPushButton("Disconnect")
        self.disconnect_button.setStyleSheet(button_stylesheet)
        self.disconnect_button.clicked.connect(self.disconnect_from_serial)
        button_layout.addWidget(self.disconnect_button)

        self.clear_button = QPushButton("Clear")
        self.clear_button.setStyleSheet(button_stylesheet)
        self.clear_button.clicked.connect(self.clear_cmd)
        button_layout.addWidget(self.clear_button)

        flo.addRow(button_layout)

        # Thêm vùng Serial Communication
        self.serial_textedit = QTextEdit()
        self.serial_textedit.setReadOnly(True)
        flo.addRow("Serial Communication:", self.serial_textedit)

        # Thêm layout vào tab
        self.upload_tab_layout.addLayout(flo)
    #tab history
    def populate_history_tab(self):
        history_tab = QVBoxLayout()

        self.reset_history_button = QPushButton("Refresh")
        self.reset_history_button.clicked.connect(self.refresh_history)
        history_tab.addWidget(self.reset_history_button)

        self.history_table = QTableWidget()
        self.history_table.setColumnCount(8)
        self.history_table.setHorizontalHeaderLabels(["Time", "File Name", "Code Size", "SF", "BW", "CR", "App Version Main" , "App Version Sub"])
        history_tab.addWidget(self.history_table)

        self.history_tab_layout.addLayout(history_tab)
    def populate_author_info_tab(self):
        # Author info tab content
        author_info_label = QLabel("Author: Nguyễn Quang Bình:       Email:20119063@hcmute.edu.vn")
        self.author_info_layout.addWidget(author_info_label)
    def handle_data(self , data):
        self.serial_textedit.append(data)
    def show_message_box(title, message, icon=QMessageBox.Information):
        msg_box = QMessageBox()
        msg_box.setWindowTitle(title)
        msg_box.setText(message)
        msg_box.setIcon(icon)
        msg_box.exec_()   
    def refresh_history(self):
        logging.basicConfig(filename='warning.log', level=logging.WARNING, format='%(asctime)s - %(levelname)s - %(message)s')
        json_file_path = r"local_appdata\fw_his.json"
        self.history_table.clearContents()
        self.history_table.setRowCount(0)
        if os.path.exists(json_file_path):
            if os.path.getsize(json_file_path) > 0:
                try:
                    with open(json_file_path, "r") as file:
                        history_data = json.load(file)
                        for entry in history_data:
                            time = str(entry['time'])
                            file_name = entry['file_name']
                            code_size = entry['code_size']
                            sf = int(entry['sf'])
                            bw = int(entry['bw'])
                            cr = int(entry['cr'])
                            app_version_main = int(entry['app_version_main'])
                            app_version_sub = int(entry['app_version_sub'])
                            row_position = self.history_table.rowCount()
                            self.history_table.insertRow(row_position)
                            self.history_table.setItem(row_position, 0, QTableWidgetItem(time))
                            self.history_table.setItem(row_position, 1, QTableWidgetItem(file_name))
                            self.history_table.setItem(row_position, 2, QTableWidgetItem(str(code_size)))
                            self.history_table.setItem(row_position, 3, QTableWidgetItem(str(sf)))
                            self.history_table.setItem(row_position, 4, QTableWidgetItem(str(bw)))
                            self.history_table.setItem(row_position, 5, QTableWidgetItem(str(cr)))
                            self.history_table.setItem(row_position, 6, QTableWidgetItem(str(app_version_main)))
                            self.history_table.setItem(row_position, 7, QTableWidgetItem(str(app_version_sub)))
                except FileNotFoundError as e:
                    logging.warning(f"File Not Found: {e}")
            else:
                logging.warning(f"Json Empty File !!")
        else:
            default_data = []
            with open(json_file_path, "w") as file:
                json.dump(default_data, file)
            self.show_message_box("Information", "No history data found.", QMessageBox.Information)
            logging.warning(f"New File Json Created : {e}")
    
    def upload_file(self):
        # Run File_Dialog
        Firebase_Uploader().hide()
        file_dialog = MainConfigFUOTA()
        #file_dialog.setNameFilter("Binary Files (*)")
        #file_dialog.setViewMode(QFileDialog.List)
        global firebase_instance
        global user

        if file_dialog.exec_():
            file_paths = file_dialog.file_info_tab.file_path
            file_name , file_code , sf, bw, cr , app_version_main , app_version_sub ,ADR_Mode , node_addr, status_update= file_dialog.get_file_info()
            if file_paths:
                download_url = None
                # Prompt the user to enter a name for the file      
                if file_name is not None and app_version_sub is not None and app_version_main is not None:
                    try:
                        app_version_sub = int(app_version_sub)
                        app_version_main = int(app_version_main)
                    except ValueError:
                        QMessageBox.warning(self, "Error", "App version must be a number.")
                        return  # Exit the function if app version is not a number
                    #firebase_instance = firebase.FirebaseInit()
                    # Create and show the progress dialog
                    progress_dialog = QProgressDialog("Uploading file...", "Cancel", 0, 0, self)
                    progress_dialog.setWindowTitle("Uploading")
                    progress_dialog.setWindowModality(Qt.WindowModal)
                    progress_dialog.setAutoClose(True)
                    progress_dialog.setAutoReset(False)
                    progress_dialog.setMinimumDuration(0)
                    progress_dialog.setValue(0)
                    # Start a separate thread to handle file upload and CRC calculation
                    thread = QThread()
                    worker = Worker(file_paths, firebase_instance  , file_name, download_url , user , app_version_main ,app_version_sub,  sf, bw, cr , file_code , ADR_Mode , node_addr , status_update) #pass the file name 
                    worker.moveToThread(thread)

                    # Connect signals
                    worker.progress.connect(progress_dialog.setValue)
                    worker.finished.connect(progress_dialog.reset)
                    worker.finished.connect(thread.quit)
                    worker.finished.connect(worker.deleteLater)
            
                    # Connect the signal to update the CRC field
                    worker.crc_calculated.connect(self.update_crc_field)

                    thread.started.connect(worker.process)
                    # Start the thread
                    thread.start()

                    # Show the progress dialog
                    progress_dialog.exec_()
                    
                Firebase_Uploader().show()
            else :  QMessageBox.warning(self, "Error", "Please chose file bin")

    def update_crc_field(self, crc_value):
        # Update the CRC field with the calculated value
        self.e1.setText(str(crc_value))
    def show_popup(self, title, message):
        msg = QMessageBox()
        msg.setWindowTitle(title)
        msg.setText(message)
        msg.exec_()
    def clear_cmd(self):
        self.serial_textedit.clear()
    def showEvent(self, event):
        # Overriding showEvent to populate serial ports combo box when the widget is shown
        self.populate_serial_ports()
    def populate_serial_ports(self):
        # Populate the serial port combo box with available ports
        self.serial_port_combobox.clear()
        ports = serial.tools.list_ports.comports()
        for port in ports:
            self.serial_port_combobox.addItem(port.device)

    def connect_to_serial(self):
        port = self.serial_port_combobox.currentText()
        baudrate = int(self.baudrate_combobox.currentText())
        success, message = self.serial_reader.connect_to_serial(port, baudrate)
        self.serial_textedit.append(message)
        if success:
            self.connect_button.setEnabled(False)
            self.disconnect_button.setEnabled(True)
   
    
    def read_serial(self):
        while (self.alive.is_set() and self.serialInst.is_open):
            data = self.serialInst.readline().decode("utf-8").strip()
            self.serial_textedit.append(data+"\n")
            if(len(data)>1):
                self.data_available.emit(data)
            self.serial_textedit.append(data)
            print(data) 

    def disconnect_from_serial(self):
        message = self.serial_reader.disconnect_from_serial()
        self.serial_textedit.append(message)
        self.connect_button.setEnabled(True)
        self.disconnect_button.setEnabled(False)
#-------------------------------FUNC of Serial
class SerialReader(QObject):
    data_available = pyqtSignal(str)

    def __init__(self, parent=None):
        super().__init__(parent)
        self.alive = Event()
        self.serialInst = serial.Serial()
        self.thread_serial = None
    def set_serial_instance(self, serial_inst):
        self.serialInst = serial_inst

    def read_serial(self):
        while self.alive.is_set() and self.serialInst.is_open:
            try:
                # Read raw bytes from the serial port
                raw_data = self.serialInst.readline()
                
                # Detect encoding of the raw data
                result = chardet.detect(raw_data)
                encoding = result['encoding'] or 'utf-8'  # Fallback to 'utf-8' if detection fails

                # Decode the raw data with detected encoding, ignoring errors
                data = raw_data.decode(encoding, errors='ignore').strip()

                if data:
                    self.data_available.emit(data)
                print(data)
            except serial.SerialException as e:
                print(f"SerialException: {e}")
                self.alive.clear()
            except Exception as e:
                print(f"Exception: {e}")
                self.alive.clear()

    def start_thread(self):
        self.thread_serial = Thread(target=self.read_serial, daemon=True)
        self.alive.set()
        self.thread_serial.start()

    def stop_thread(self):
        if self.thread_serial is not None:
            self.alive.clear()
            self.thread_serial.join(timeout=1)
            if self.thread_serial.is_alive():
                print("Warning: Serial reading thread did not terminate in time.")
            self.thread_serial = None

    def connect_to_serial(self, port, baudrate):
        try:
            self.serialInst.baudrate = baudrate
            self.serialInst.port = port
            self.serialInst.open()
            if self.serialInst.is_open:
                self.start_thread()
                return True, f"Connected to {port} at {baudrate} baud rate."
        except serial.SerialException as e:
            return False, f"Failed to connect to {port}: {str(e)}"
        return False, "Unknown error occurred."

    def disconnect_from_serial(self):
        self.stop_thread()
        if self.serialInst.is_open:
            self.serialInst.close()
        return "Disconnected Port success."

#---------------------------------------Update Info ----------------------------------
class MainConfigFUOTA(QDialog):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Configurate FUOTA")
        self.setGeometry(100, 100, 800, 600)
        self.file_path = None
        # Create the tabs
        self.tab_widget = QTabWidget()
        self.file_info_tab = FileInputDialog(self.file_path)
        self.device_management_tab = DeviceManagementTab()
        self.tab_widget.addTab(self.file_info_tab, "File Information")
        self.tab_widget.addTab(self.device_management_tab, "Device Management")
        self.ok_button = QPushButton("OK")
        self.ok_button.setStyleSheet("background-color: #4CAF50; color: white;")  # Green button
        self.ok_button.clicked.connect(self.accept)
        self.cancel_button = QPushButton("Cancel")
        self.cancel_button.setStyleSheet("background-color: #f44336; color: white;")  # Red button
        self.cancel_button.clicked.connect(self.reject)
        # Main layout
        button_layout = QHBoxLayout()
        button_layout.addWidget(self.ok_button)
        button_layout.addWidget(self.cancel_button)
        main_layout = QVBoxLayout()
        main_layout.addWidget(self.tab_widget)
        main_layout.addLayout(button_layout)
        self.setLayout(main_layout)
    def get_file_info(self):
        if self.file_info_tab.file_path != None:
            file_name = self.file_info_tab.file_name_edit.text()
            if self.file_info_tab.contains_special_characters(file_name):
                QMessageBox.warning(self, "Invalid Filename", "Filename cannot contain special characters. Please try again.")
                return None, None, None, None, None, None
             # Convert SF, BW, and CR to integer
            sf_text = self.file_info_tab.sf_combobox.currentText()
            bw_text = self.file_info_tab.bw_combobox.currentText()
            cr_text = self.file_info_tab.cr_combobox.currentText()
            sf = self.file_info_tab.Spreading_Factor.get(sf_text, -1)  # Get SF integer value from dictionary
            bw = self.file_info_tab.BandWidth.get(bw_text, -1)  # Get BW integer value from dictionary
            cr = self.file_info_tab.LoRaCR.get(cr_text, -1)  # Get CR integer value from dictionary
            file_code = self.file_info_tab.file_size
            app_version_main = self.file_info_tab.app_version_main_edit.value()
            app_version_sub = self.file_info_tab.app_version_sub_edit.value()
            ADR_Mode = self.file_info_tab.ADR_mode
            node_address = self.file_info_tab.node_address_combobox.currentText()
            status_update = self.file_info_tab.UpdateStatus_val
            return file_name, file_code, sf, bw, cr, app_version_main , app_version_sub ,ADR_Mode,node_address,status_update
        else:
            return None, None, None, None, None, None , None, None, None,None
# Custom class combobox
class CustomComboBox(QComboBox):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.load_function = None

    def set_load_function(self, load_function):
        self.load_function = load_function

    def showPopup(self):
        if self.load_function:
            self.load_function()
        super().showPopup()
class FileInputDialog(QDialog):
    def __init__(self, file_path, parent=None):
        super().__init__()
        self.setWindowTitle("File Information")
        self.file_path = file_path
        self.ADR_mode = False
        self.UpdateStatus_val = "false"
        self.Spreading_Factor = {
            '6':   0, 
            '7':   1,
            '8':   2,
            '9':   3,
            '10':  4,
            '11':  5,
            '12':  6
        }

        self.BandWidth = {
            '7.8 kHz':   0, 
            '10.4 kHz':  1,
            '15.6 kHz':  2,
            '20.8 kHz':  3,
            '31.2 kHz':  4,
            '41.7 kHz':  5,
            '62.5 kHz':  6,
            '125 kHz':   7, 
            '250 kHz':   8,
            '500 kHz':   9
        }

        self.LoRaCR = {
            '4/5': 0, 
            '4/6': 1,
            '4/7': 2,
            '4/8': 3
        }

        self.file_size = None
        self.file_name_label = QLabel("File Name:")
        self.file_name_edit = QLineEdit()
        self.file_name_edit.setReadOnly(False)

        self.file_dir_label = QLabel("File Directory:")
        self.file_dir_edit = QLineEdit()
        self.file_browse_button = QPushButton("Browse")
        self.file_browse_button.setStyleSheet("QPushButton { font-weight: bold; }")
        self.file_browse_button.clicked.connect(self.browse_file)
        self.file_browse_button.setIcon(QIcon(r"img\upload_file.png"))
        self.file_name_layout = QHBoxLayout()
        self.file_name_layout.addWidget(self.file_dir_label)
        self.file_name_layout.addWidget(self.file_dir_edit)
        self.file_name_layout.addWidget(self.file_browse_button)
        self.file_dir_edit.setReadOnly(True)

        self.file_size_layout = QHBoxLayout()
        self.file_size_label = QLabel("File Size:")
        self.file_size_code_edit = QLineEdit()
        self.file_size_layout.addWidget(self.file_size_label)
        self.file_size_layout.addWidget(self.file_size_code_edit)
        self.file_size_code_edit.setReadOnly(True)

        self.config_mode_parameter_layout = QHBoxLayout()
        self.config_mode_parameter = QLabel("Enable ADR (Adaptive Data rate)")
        self.config_mode_checkbox_parameter = QCheckBox()
        self.config_mode_checkbox_parameter.clicked.connect(self.config_mode_ADR)
        self.config_mode_parameter_layout.addWidget(self.config_mode_checkbox_parameter)
        self.config_mode_parameter_layout.addWidget(self.config_mode_parameter)

        self.config_mode_notify = QLabel("* Notice: Enabling ADR mode will disable manual configuration of LoRa parameters")
        self.config_mode_notify.setStyleSheet("color: red;")

        self.Update_Status = QLabel("* Notice: Start update after upload firmware successfully")
        self.Update_Status.setStyleSheet("color: red;")
        self.Update_Status_check = QCheckBox()
        self.Update_Status_check.clicked.connect(self.config_update)
        self.Update_Status_check_text = QLabel("Start Update")
        self.Update_Status_check_layout = QHBoxLayout()
        self.Update_Status_check_layout.addWidget(self.Update_Status_check)
        self.Update_Status_check_layout.addWidget(self.Update_Status_check_text)
        self.Update_Status_check_layout.setSpacing(5)
        self.Update_Status_check_layout.setContentsMargins(0, 0, 0, 0)

        self.Update_Status_layout = QVBoxLayout()
        self.Update_Status_layout.addWidget(self.Update_Status)
        self.Update_Status_layout.addLayout(self.Update_Status_check_layout)

        self.sf_label = QLabel("Spreading Factor:")
        self.sf_combobox = QComboBox()
        self.sf_combobox.addItems(self.Spreading_Factor.keys())

        self.bw_label = QLabel("Bandwidth:")
        self.bw_combobox = QComboBox()
        self.bw_combobox.addItems(self.BandWidth.keys())

        self.cr_label = QLabel("Coding Rate:")
        self.cr_combobox = QComboBox()
        self.cr_combobox.addItems(self.LoRaCR.keys())

        self.app_version_main_label = QLabel("App Version Main:")
        self.app_version_main_edit = QSpinBox()
        self.app_version_main_edit.setMinimum(0)
        self.app_version_main_edit.setMaximum(9999)

        self.app_version_sub_label = QLabel("App Version Sub:")
        self.app_version_sub_edit = QSpinBox()
        self.app_version_sub_edit.setMinimum(0)
        self.app_version_sub_edit.setMaximum(9999)

        # Create a horizontal layout for the app versions
        self.app_version_layout = QHBoxLayout()
        self.app_version_layout.addWidget(self.app_version_main_label)
        self.app_version_layout.addWidget(self.app_version_main_edit)
        self.app_version_layout.addWidget(self.app_version_sub_label)
        self.app_version_layout.addWidget(self.app_version_sub_edit)

        self.node_address_label = QLabel("Node Address:")
        self.node_address_combobox = CustomComboBox()
        self.node_address_combobox.set_load_function(self.load_node_addresses)
        self.load_node_addresses()

        layout = QVBoxLayout()
        layout.addWidget(self.node_address_label)
        layout.addWidget(self.node_address_combobox)
        layout.addLayout(self.file_name_layout)
        layout.addLayout(self.file_size_layout)
        layout.addWidget(self.file_name_label)
        layout.addWidget(self.file_name_edit)
        layout.addWidget(self.sf_label)
        layout.addWidget(self.sf_combobox)
        layout.addWidget(self.bw_label)
        layout.addWidget(self.bw_combobox)
        layout.addWidget(self.cr_label)
        layout.addWidget(self.cr_combobox)
        layout.addLayout(self.app_version_layout)  # Add the app version layout
        layout.addLayout(self.Update_Status_layout)

        self.setLayout(layout)

        # Apply style
        self.apply_styles()
    
    def apply_styles(self):
        # Button hover effect
        self.file_browse_button.setStyleSheet("""
            QPushButton:hover { background-color: lightgray; }
        """)
        # Font weight for labels
        labels = self.findChildren(QLabel)
        for label in labels:
            label.setStyleSheet("font-weight: bold;")
    def browse_file(self):
        self.file_path, _ = QFileDialog.getOpenFileName(self, "Choose Firmware File", "", "Bin Files (*.bin)")
        if self.file_path:
            self.file_size = os.path.getsize(self.file_path)
            if self.file_size > 44 * 1024:
                QMessageBox.warning(self, "File Size Error", "The selected file is larger than 42KB.")
                return  # Exit the function if file size is too large

            base_name = os.path.basename(self.file_path)  # Lấy tên gốc của file
            base_name_without_extension = os.path.splitext(base_name)[0]  # Loại bỏ phần mở rộng .bin
            self.file_name_edit.setText(base_name_without_extension)
            self.file_dir_edit.setText(self.file_path)
            self.file_size_code_edit.setText(str(self.file_size))
    #Load Node Address
    def load_node_addresses(self):
        try:
            #clear before load 
            self.node_address_combobox.clear()
            with open('devices.json', 'r') as f:
                devices = json.load(f)
                if devices:
                    for device in devices:
                        self.node_address_combobox.addItem(device["node_address"])
                else:
                    QMessageBox.warning(self, "Warning", "Please add device.")
        except (FileNotFoundError, json.JSONDecodeError) as e:
            QMessageBox.warning(self, "Warning", "Please add device.")
            with open('app.log', 'a') as log_file:
                log_file.write(f"{e}\n")
    #ConfigParamenter Mode 
    #ConfigADR mode
    def config_mode_ADR(self):
        if self.config_mode_checkbox_parameter.isChecked():
            self.sf_combobox.setDisabled(True)
            self.sf_combobox.setCurrentIndex(6)
            self.bw_combobox.setDisabled(True)
            self.bw_combobox.setCurrentIndex(7)
            self.cr_combobox.setDisabled(True)
            self.cr_combobox.setCurrentIndex(3)
            self.ADR_mode = True
        else: 
            self.sf_combobox.setDisabled(False)
            self.cr_combobox.setDisabled(False)
            self.bw_combobox.setDisabled(False)
            self.ADR_mode = False
    def config_update(self):
        if self.Update_Status_check.isChecked():
            self.UpdateStatus_val  = "true"
        else : self.UpdateStatus_val  = "false"
    # Define a function to check if the filename contains special characters
    def contains_special_characters(self , filename):
        # Define a regular expression pattern to match special characters
        pattern = r'[!@#$%^&*(),.?":{}|<>]'
        # Use the search function to check if the pattern matches any part of the filename
        if re.search(pattern, filename):
            return True  # Special characters found
        else:
            return False  # No special characters found
class DeviceManagementTab(QWidget):
    def __init__(self):
        super().__init__()

        # Widgets for adding a new device
        self.device_name_label = QLabel("Device Name:")
        self.device_name_edit = QLineEdit()
        self.node_address_label = QLabel("Node Address:")
        self.node_address_edit = QLineEdit()
        self.add_device_button = QPushButton("Add Device")
        self.add_device_button.clicked.connect(self.add_device)
        # Table to display existing devices
        self.device_table = QTableWidget()
        self.device_table.setColumnCount(3)  # Thêm một cột mới cho nút xóa
        self.device_table.setHorizontalHeaderLabels(["Device Name", "Node Address", "Actions"])
        # Layout for adding a new device
        add_device_layout = QHBoxLayout()
        add_device_layout.addWidget(self.device_name_label)
        add_device_layout.addWidget(self.device_name_edit)
        add_device_layout.addWidget(self.node_address_label)
        add_device_layout.addWidget(self.node_address_edit)
        add_device_layout.addWidget(self.add_device_button)
        # Main layout
        main_layout = QVBoxLayout()
        main_layout.addLayout(add_device_layout)
        main_layout.addWidget(self.device_table)

        self.setLayout(main_layout)
        # Load devices from JSON file
        self.load_devices()
    def save_devices(self):
        devices = []
        for row in range(self.device_table.rowCount()):
            device_name = self.device_table.item(row, 0).text()
            node_address = self.device_table.item(row, 1).text()
            devices.append({"device_name": device_name, "node_address": node_address})

        with open('devices.json', 'w') as f:
            json.dump(devices, f, indent=4)
    def add_device(self):
        device_name = self.device_name_edit.text()
        node_address = self.node_address_edit.text()
        
        if not device_name or not node_address:
            QMessageBox.warning(self, "Warning", "Device name and Node address cannot be empty.")
            return

        if not self.is_valid_device_name(device_name):
            QMessageBox.warning(self, "Warning", "Device name cannot contain special characters.")
            return

        if not self.is_valid_node_address(node_address):
            QMessageBox.warning(self, "Warning", "Node address is not valid.")
            return

        for row in range(self.device_table.rowCount()):
            existing_name = self.device_table.item(row, 0).text()
            existing_address = self.device_table.item(row, 1).text()
            if device_name == existing_name:
                QMessageBox.warning(self, "Warning", "Device name already exists.")
                return
            if node_address == existing_address:
                QMessageBox.warning(self, "Warning", "Node address already exists.")
                return

        row_position = self.device_table.rowCount()
        self.device_table.insertRow(row_position)
        self.device_table.setItem(row_position, 0, QTableWidgetItem(device_name))
        self.device_table.setItem(row_position, 1, QTableWidgetItem(node_address))
        remove_button = QPushButton("Remove")
        remove_button.clicked.connect(lambda _, r=row_position: self.remove_device(r))
        self.device_table.setCellWidget(row_position, 2, remove_button)
        self.device_name_edit.clear()
        self.node_address_edit.clear()
        self.save_devices()
    def load_devices(self):
        if not os.path.exists('devices.json'):
            # Create an empty JSON file if it doesn't exist
            with open('devices.json', 'w') as f:
                json.dump([], f)
            logging.warning("devices.json file not found. A new file has been created.")      
        try:
            with open('devices.json', 'r') as f:
                devices = json.load(f)
                for device in devices:
                    row_position = self.device_table.rowCount()
                    self.device_table.insertRow(row_position)
                    self.device_table.setItem(row_position, 0, QTableWidgetItem(device["device_name"]))
                    self.device_table.setItem(row_position, 1, QTableWidgetItem(device["node_address"]))
        except (FileNotFoundError, json.JSONDecodeError) as e:
            logging.warning(f"Error loading devices.json: {e}")
            QMessageBox.warning(self, "Error", "There was an issue loading the devices file.")
         # Thêm nút xóa vào mỗi hàng của bảng
        for row in range(self.device_table.rowCount()):
            remove_button = QPushButton("Remove")
            remove_button.clicked.connect(lambda _, r=row: self.remove_device(r))  # Sử dụng lambda để truyền tham số hàng
            self.device_table.setCellWidget(row, 2, remove_button)
    def remove_device(self, row):
        # Xác định thiết bị được chọn và xóa nó khỏi bảng
        self.device_table.removeRow(row)
        self.save_devices()
    def is_valid_device_name(self, name):
        # Kiểm tra xem tên có chứa ký tự đặc biệt hay không
        return not bool(re.search(r'[^a-zA-Z0-9\s]', name))

    def is_valid_node_address(self , address):
        # Kiểm tra xem địa chỉ có chứa ký tự đặc biệt hay không
        return not bool(re.search(r'[^a-fA-F0-9]', address))
#-------------------------------SIGN UP PAGE ------------------------------------------------
class SignupPage(QWidget):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Signup Page")
        self.setGeometry(200, 200, 500, 500)
        layout = QVBoxLayout()
        self.username_label = QLabel("Username:")
        self.username_input = QLineEdit()
        layout.addWidget(self.username_label)
        layout.addWidget(self.username_input)
        self.email_label = QLabel("Email:")
        self.email_input = QLineEdit()
        layout.addWidget(self.email_label)
        layout.addWidget(self.email_input)
        self.password_label = QLabel("Password:")
        self.password_input = QLineEdit()
        self.password_input.setEchoMode(QLineEdit.Password)
        layout.addWidget(self.password_label)
        layout.addWidget(self.password_input)
        self.signup_button = QPushButton("Sign Up")
        self.signup_button.clicked.connect(firebase.FirebaseInit.signup(self , self.email_input , self.password_input))
        layout.addWidget(self.signup_button)
        self.setLayout(layout)


if __name__ == "__main__":
    app = QApplication(sys.argv)
    ## image for info
    INFO_ICON = QIcon(QPixmap(r"img\info_icon.png"))
    WARNING_ICON = QIcon(QPixmap(r"img\warning_icon.png"))
    serial_port = QSerialPort()
    app.setStyle('Fusion')
    window = LoginPage()
    window.show()
    sys.exit(app.exec_())

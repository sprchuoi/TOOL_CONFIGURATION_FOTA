import sys
from PyQt5.QtWidgets import *
from PyQt5.QtGui import *
from PyQt5.QtCore import *
import fb_connect_storage as firebase
from PyQt5.QtWebEngineWidgets import QWebEngineView
from datetime import datetime 
import json
import hashlib
import serial
import serial.tools.list_ports
from threading import Thread , Event
import os
import logging
import re  # Import the regular expression module
#global variables
custom_crc_table = {}
poly = 0x04C11DB7
crc32_checksum = 0x00000000
user  = None
firebase_instance = None
FW_information = None 
cred_path = "TOOL_CONFIGURATION_FOTA/serviceAccount.json"
#------------------ Create thread for update ------------------------
class Worker(QObject):
    progress = pyqtSignal(int)
    finished = pyqtSignal()
    crc_calculated = pyqtSignal(str)  # New signal to emit the calculated CRC
    def __init__(self, file_path, firebase_instance , file_name , download_url , user_current , app_version , sf, bw, cr , file_code):
        super().__init__()
        self.file_path = file_path
        self.firebase_instance = firebase_instance
        self.file_name = file_name  # Store the file name
        self.download_url  = download_url 
        self.user_current = user_current
        self.app_version  = app_version
        self.sf = sf 
        self.bw = bw
        self.cr = cr
        self.file_code = file_code
        self.node_id   = 0x01
        global FW_information
    def process(self):
        # upload to firebase
        self.firebase_instance.fb_add_file(self.file_path, self.file_name)
        #get Url 
        self.download_url = self.firebase_instance.getDownload_URL( self.file_name , user)
        #push information to realtime database 
        
        time_current_update  = datetime.now()
        time_current_save  = time_current_update.strftime("%d/%m/%Y-%H:%M:%S")
        upload_date_str = time_current_update.strftime("%Y%m%d%H%M%S")
        #convert dict to JSON 
        FW_Information = self.Create_Json_Object(self.download_url  , time_current_update , self.app_version)
        print(FW_Information)
        #upload to FIrebase
        self.firebase_instance.set_FW_path(self.file_name ,self.download_url)
        self.firebase_instance.set_App_ver(self.file_name , self.app_version)
        self.firebase_instance.set_timedate(self.file_name , upload_date_str)
        self.firebase_instance.set_file_code(self.file_name , self.file_code)
        self.firebase_instance.set_LoRa_info(self.file_name , self.sf , self.bw , self.cr)
        self.firebase_instance.set_node_update(self.file_name , self.node_id)
        self.save_to_json(time_current_save, self.file_name,self.file_code ,self.sf, self.bw, self.cr , self.app_version )
        #print(self.download_url)
        self.generate_crc32_table(poly)
        crc32_checksum = self.crc32_stm(self.file_path)
        #print(crc32_checksum)
        self.crc_calculated.emit(crc32_checksum)  # Emit the calculated CRC
        self.show_popup("Upload Success" , "Upload Status" )
        self.finished.emit()
    @staticmethod
    def show_popup(title, message):
        msg = QMessageBox()
        msg.setWindowTitle(title)
        msg.setText(message)
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
        crc = hex(crc)
        return crc
    def Create_Json_Object(self , url, upload_date , app_version):
        #convert Datetime
        upload_date_str = upload_date.strftime("%Y-%m-%d %H:%M:%S")
        FW_information ={
            "URL" :url,
            "UploadDate" : upload_date_str,
            "Appversion" : app_version
        }
        #convert dictionary to JSON 
        json_object = json.dumps(FW_information)
        return json_object
    #save to Json 
    
    def save_to_json(self, time, file_name, code_size, sf, bw, cr, app_version):
        json_file_path = r"local_appdata\fw_his.json"

        # Create a dictionary for the new entry
        file_info = {
            "time": time,
            "file_name": str(file_name),
            "code_size": int(code_size),
            "sf": sf,
            "bw": bw,
            "cr": cr,
            "app_version": app_version
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
        self.setGeometry(200, 200, 500, 300)

        main_layout = QVBoxLayout()

        # Title label
        title_label = QLabel("Login", self)
        title_label.setStyleSheet("font-size: 24px; font-weight: bold; color: #333;")
        main_layout.addWidget(title_label, alignment=Qt.AlignCenter)
        # FOTA-LORA label
        fota_label = QLabel("FOTA-LORA", self)
        fota_label.setStyleSheet("font-size: 18px; font-weight: bold; color: blue;")
        main_layout.addWidget(fota_label, alignment=Qt.AlignCenter)

        # Username input
        self.username_label = QLabel("Username:", self)
        self.username_input = QLineEdit(self)
        self.username_input.setPlaceholderText("Enter your username")
        self.username_input.setStyleSheet("padding: 10px; border: 1px solid #ccc; border-radius: 5px;")
        self.username_input.installEventFilter(self)  # Enable event filter for username input
        main_layout.addWidget(self.username_label)
        main_layout.addWidget(self.username_input)

        # Password input
        self.password_label = QLabel("Password:", self)
        self.password_input = QLineEdit(self)
        self.password_input.setPlaceholderText("Enter your password")
        self.password_input.setEchoMode(QLineEdit.Password)
        self.password_input.setStyleSheet("padding: 10px; border: 1px solid #ccc; border-radius: 5px;")
        self.password_input.installEventFilter(self)  # Enable event filter for password input
        main_layout.addWidget(self.password_label)
        main_layout.addWidget(self.password_input)

        # Buttons layout
        button_layout = QHBoxLayout()

        # Login button
        self.login_button = QPushButton(self)
        self.login_button.setIcon(QIcon("img/login.png"))  # Đường dẫn đến biểu tượng của bạn
        self.login_button.setText("Login")  # Thêm văn bản vào nút
        self.login_button.setStyleSheet("padding: 10px 20px; border: none; border-radius: 5px; background-color: #007BFF; color: white;")
        self.login_button.clicked.connect(self.login)
        self.login_button.installEventFilter(self)  # Enable event filter for login button
        button_layout.addWidget(self.login_button)

        # Signup button
        self.signup_button = QPushButton(self)
        self.signup_button.setIcon(QIcon("img/signup.png"))  # Đường dẫn đến biểu tượng của bạn
        self.signup_button.setText("Sign up")  # Thêm văn bản vào nút
        self.signup_button.setStyleSheet("padding: 10px 20px; border: none; border-radius: 5px; background-color: #28A745; color: white;")
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
        if event.type() == QEvent.Type.MouseMove:  # Sự kiện di chuột qua
            if obj == self.username_input or obj == self.password_input:
                obj.setStyleSheet("padding: 10px; border: 1px solid #007BFF; border-radius: 5px;")
        elif event.type() == QEvent.Type.Leave:  # Sự kiện di chuột ra
            if obj == self.username_input or obj == self.password_input:
                obj.setStyleSheet("padding: 10px; border: 1px solid #ccc; border-radius: 5px;")
        if event.type() == QEvent.MouseButtonPress:
            if obj == self.login_button or obj == self.signup_button:
                obj.setStyleSheet("padding: 10px 20px; border: none; border-radius: 5px; background-color: #0056b3; color: white;")
        elif event.type() == QEvent.MouseButtonRelease:
            if obj == self.login_button:
                obj.setStyleSheet("padding: 10px 20px; border: none; border-radius: 5px; background-color: #007BFF; color: white;")
            elif obj == self.signup_button:
                obj.setStyleSheet("padding: 10px 20px; border: none; border-radius: 5px; background-color: #28A745; color: white;")
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
        self.serialInst = serial.Serial()
        self.data_available = pyqtSignal(str)

        self.setWindowTitle("FUOTA-LORA")
        self.setGeometry(200, 200, 500, 500)

        self.font = QFont()
        self.font.setFamily('Arial')
        self.font.setPointSize(12)  # Giảm kích thước phông chữ

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
        self.tab_widget.setStyleSheet("QTabWidget::pane {background-color: white;} QTabWidget::tab-bar {left: 20px;} QTabWidget::tab {background-color: white; color: black;} QTabWidget::tab:selected {background-color: white; color: black;} QTabWidget::tab:hover {background-color: white;}")

        # Populate the upload tab
        self.populate_upload_tab()

        # Populate the history tab
        self.populate_history_tab()
        self.populate_author_info_tab()
        # Set main layout
        main_layout = QVBoxLayout()
        main_layout.addWidget(self.tab_widget)

        self.setLayout(main_layout)

    def populate_upload_tab(self):
        # Upload tab content
        # Add FUTOA - LORA label and logo to the form layout
        futoa_layout = QHBoxLayout()
        futoa_label = QLabel("FUOTA - LORA")
        futoa_label.setStyleSheet("color: blue;")  # Set text color to green
        futoa_label.setFont(QFont("Arial", 15, QFont.Bold))  # Set font and size
        futoa_layout.addWidget(futoa_label)
        futoa_logo = QLabel()
        pixmap = QPixmap("img/logo-hcmute-inkythuatso.jpg")  # Đường dẫn của file hình ảnh
        pixmap = pixmap.scaled(100, 120)  # Resize pixmap to 30x30 pixels
        futoa_logo.setPixmap(pixmap)
        futoa_layout.addWidget(futoa_logo)
        self.upload_tab_layout.addLayout(futoa_layout)

        flo = QFormLayout()

        # Upload file button
        self.upload_button = QPushButton("Upload File")
        self.upload_button.setStyleSheet("background-color: orange; color: black;")  # Thêm màu sắc cho nút
        self.upload_button.clicked.connect(self.upload_file)
        flo.addRow(self.upload_button)

        # CRC, Version, and Date Release fields
        self.e1 = QLineEdit()
        self.e1.setValidator(QIntValidator())
        self.e1.setMaxLength(4)
        flo.addRow("CRC:", self.e1)
       # Serial connection section
        self.serial_port_label = QLabel("Serial Port:")
        self.serial_port_combobox = QComboBox()
        flo.addRow(self.serial_port_label, self.serial_port_combobox)

        # Update port button
        self.update_port = QPushButton("Update Ports")
        self.update_port.setStyleSheet("background-color: #008CBA; color: black;")  # Thêm màu sắc cho nút
        self.update_port.clicked.connect(self.populate_serial_ports)
        flo.addWidget(self.update_port)

        # Select Baudrate
        self.baudrate_label = QLabel("Baud Rate:")
        self.baudrate_combobox = QComboBox()
        self.baudrate_combobox.setEditable(False)  # Disable manual input
        self.baudrate_combobox.addItems(["4800", "9600", "19200", "38400", "57600", "115200"])  # Add your desired baud rates
        flo.addRow(self.baudrate_label, self.baudrate_combobox)

        # Connect, Disconnect, and Clear buttons
        button_layout = QHBoxLayout()
        self.connect_button = QPushButton("Connect")
        self.connect_button.setStyleSheet("background-color: green; color: black;")
        self.connect_button.clicked.connect(self.connect_to_serial)
        button_layout.addWidget(self.connect_button)

        self.disconnect_button = QPushButton("Disconnect")
        self.disconnect_button.setStyleSheet("background-color: red; color: black;")
        self.disconnect_button.clicked.connect(self.disconnect_from_serial)
        button_layout.addWidget(self.disconnect_button)

        self.clear_button = QPushButton("Clear")
        self.clear_button.setStyleSheet("background-color: gray; color: white;")
        self.clear_button.clicked.connect(self.clear_cmd)
        button_layout.addWidget(self.clear_button)

        flo.addRow(button_layout)

        # Text area to display serial communication
        self.serial_textedit = QTextEdit()
        self.serial_textedit.setReadOnly(1)
        flo.addRow("Serial Communication:", self.serial_textedit)

        self.upload_tab_layout.addLayout(flo)
        self.serial_read_thread = None
        self.thread = None
        self.alive = Event()
    def populate_history_tab(self):
         # Reset history button
        history_tab = QFormLayout()

        self.refresh_history
        self.reset_history_button = QPushButton("Refresh")
        self.reset_history_button.clicked.connect(self.refresh_history)
        # History tab content
        history_tab.addRow(self.reset_history_button)
        self.history_Textedit = QTextEdit()
        self.history_Textedit.setReadOnly(1)
        history_tab.addRow(self.history_Textedit)
        self.history_tab_layout.addLayout(history_tab)
    def refresh_history(self):
        # Configure logging to output warnings to a file
        logging.basicConfig(filename='warning.log', level=logging.WARNING, format='%(asctime)s - %(levelname)s - %(message)s')
        json_file_path = r"local_appdata\fw_his.json"
        # Clear existing widgets in history_tab_layout
        self.history_Textedit.clear
        if os.path.exists(json_file_path):
            if os.path.getsize(json_file_path) > 0:  # Check if the file is not empty
                # Read the JSON data from the file
                try:
                    with open(json_file_path, "r") as file:
                        history_data = json.load(file)
                        for entry in history_data:
                            # Iterate over the history data and create labels to display the information
                            time = str(entry['time'])
                            file_name = entry['file_name']
                            code_size = entry['code_size']
                            sf = int(entry['sf'])
                            bw = int(entry['bw'])
                            cr = int(entry['cr'])
                            app_version = entry['app_version']
                            info_str = f"{time}: File: {file_name}, Code: {code_size}, SF: {sf}, BW: {bw}, CR: {cr}, App Version: {app_version}"
                            self.history_Textedit.append(info_str)
                except FileNotFoundError as e:
                    # Log the warning to the file
                    logging.warning(f"File Not Found: {e}")
                    # Handle the case where the file does not exist
                    #QMessageBox.warning(self, "File Not Found", "History data file not found.")
            else:
                #QMessageBox.information(self, "Empty File", "The history data file is empty.")
                logging.warning(f"Json Empty File !!")
        else:
            # Create a new JSON file with default content
            default_data = []
            with open(json_file_path, "w") as file:
                json.dump(default_data, file)
            # Inform the user that a new file has been created
            #QMessageBox.information(self, "New File Created", "A new history data file has been created.")
            logging.warning(f"New File Json Created : {e}")
    def populate_author_info_tab(self):
        # Author info tab content
        author_info_label = QLabel("Author: Nguyễn Quang Bình:       Email:20119063@hcmute.edu.vn")
        self.author_info_layout.addWidget(author_info_label)
    def upload_file(self):
        file_dialog = QFileDialog(self)
        file_dialog.setNameFilter("Binary Files (*.bin)")
        file_dialog.setViewMode(QFileDialog.List)
        global firebase_instance
        global user
        if file_dialog.exec_():
            file_paths = file_dialog.selectedFiles()
            
            if file_paths:
                file_path = file_paths[0]
                download_url = None
                file_input_dialog = FileInputDialog(file_path , self)
                file_name , file_code , sf, bw, cr , app_version , = file_input_dialog.get_file_info()
                
                # Prompt the user to enter a name for the file      
                if file_name is not None and app_version is not None:
                    try:
                        app_version = int(app_version)
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
                    worker = Worker(file_path, firebase_instance  , file_name, download_url , user , app_version ,  sf, bw, cr , file_code ) #pass the file name 
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
        # Connect to serial port
        port = self.serial_port_combobox.currentText()
        baudrate = int(self.baudrate_combobox.currentText())
        try:
            self.serialInst.baudrate = baudrate
            self.serialInst.port = port
            self.serial_textedit.append(f"Connected to {port} at {baudrate} baud rate.")
            self.serialInst.open()
            #open port
            if(self.serialInst.is_open):
                self.connect_button.setEnabled(False)
                self.disconnect_button.setEnabled(True)
                self.start_thread()
            # Start reading from the serial port
        except serial.SerialException as e:
            self.serial_textedit.append(f"Failed to connect to {port}: {str(e)}")
    def update_crc_field(self, crc_value):
        # Update the CRC field with the calculated value
        self.e1.setText(str(crc_value))
    def disconnect_from_serial(self):
        self.stop_thread()
        self.serial_textedit.append("Disconnected from serial port.")
        self.connect_button.setEnabled(True)
        self.disconnect_button.setEnabled(False)
        self.serialInst.close()

    def send_Data(self, data ):
        if(self.serialInst.is_open):
            messages = str(data) + "\n"
            self.serialInst.write(messages.encode())
    def read_serial(self):
        while (self.alive.is_set() and self.serialInst.is_open):
            data = self.serialInst.readline().decode("utf-8").strip()
            self.serial_textedit.append(data+"\n")
            # if(len(data)>1):
            #     self.data_available.emit(data)
            self.serial_textedit.append(data)
            print(data) 
    def show_popup(self, title, message):
        msg = QMessageBox()
        msg.setWindowTitle(title)
        msg.setText(message)
        msg.exec_()

    # create thread 
    def start_thread(self):
        self.thread = Thread(target = self.read_serial ,daemon=True)
        self.alive.set()
        self.thread.start()
    
    def stop_thread(self):
        if(self.thread is not None):
            self.alive.clear()
            self.thread.join()
            self.thread = None
    
    

#---------------------------------------Update Info ----------------------------------
class FileInputDialog(QDialog):
    def __init__(self, file_path, parent=None):
        super().__init__(parent)
        self.setWindowTitle("File Information")
        self.file_path = file_path
        self.Spreading_Factor = {
            '6':   0, 
            '7' :  1,
            '8' :  2,
            '9' :  3,
            '10':  4,
            '11' : 5,
            '12' : 6
        }
        self.BandWidth = {
            '7.8 kHz': 0 , 
            '10.4 kHz' :  1,
            '15.6 kHz':  2,
            '20.8 kHz' : 3 ,
            '31.2 kHz' : 4,
            '41.7 kHz' : 5,
            '62.5 kHz' : 6,
            '125 kHz': 7 , 
            '250 kHz': 8,
            '500 kHz': 9
        }
        self.LoRaCR = {
            '4/5': 0 , 
            '4/6' :  1,
            '4/7':  2,
            '4/8' : 3
        }
        # Get file size
        self.file_size = os.path.getsize(file_path)

        self.file_name_label = QLabel("File Name:")
        self.file_name_edit = QLineEdit()
        self.file_name_edit.setText(os.path.basename(file_path))  # Set default file name to the file's basename
        self.file_name_edit.setReadOnly(False)  # Make the file name field read-only

        self.file_size_label = QLabel(f"File Size: {self.file_size} bytes")

        self.sf_label = QLabel("Spreading Factor:")
        self.sf_combobox = QComboBox()
        self.sf_combobox.addItems(self.Spreading_Factor)  # Add spreading factor options
        
        self.bw_label = QLabel("Bandwidth:")
        self.bw_combobox = QComboBox()
        self.bw_combobox.addItems(self.BandWidth)  # Add bandwidth options
        self.cr_label = QLabel("Coding Rate:")
        self.cr_combobox = QComboBox()
        self.cr_combobox.addItems(self.LoRaCR)  # Add coding rate options
        self.app_version_label = QLabel("App Version:")
        self.app_version_edit = QSpinBox()
        self.app_version_edit.setMinimum(0)  # Set minimum app version
        self.app_version_edit.setMaximum(9999)  # Set maximum app version
        self.ok_button = QPushButton("OK")
        self.ok_button.setStyleSheet("background-color: #4CAF50; color: white;")  # Green button
        self.ok_button.clicked.connect(self.accept)
        self.cancel_button = QPushButton("Cancel")
        self.cancel_button.setStyleSheet("background-color: #f44336; color: white;")  # Red button
        self.cancel_button.clicked.connect(self.reject)
        layout = QVBoxLayout()
        layout.addWidget(self.file_name_label)
        layout.addWidget(self.file_name_edit)
        layout.addWidget(self.file_size_label)
        layout.addWidget(self.sf_label)
        layout.addWidget(self.sf_combobox)
        layout.addWidget(self.bw_label)
        layout.addWidget(self.bw_combobox)
        layout.addWidget(self.cr_label)
        layout.addWidget(self.cr_combobox)
        layout.addWidget(self.app_version_label)
        layout.addWidget(self.app_version_edit)
        button_layout = QHBoxLayout()
        button_layout.addWidget(self.ok_button)
        button_layout.addWidget(self.cancel_button)
        layout.addLayout(button_layout)
        self.setLayout(layout)
    def get_file_info(self):
        if self.exec_() == QDialog.Accepted:
            file_name = self.file_name_edit.text()
            if self.contains_special_characters(file_name):
                QMessageBox.warning(self, "Invalid Filename", "Filename cannot contain special characters. Please try again.")
                return None, None, None, None, None, None
             # Convert SF, BW, and CR to integer
            sf_text = self.sf_combobox.currentText()
            bw_text = self.bw_combobox.currentText()
            cr_text = self.cr_combobox.currentText()
            sf = self.Spreading_Factor.get(sf_text, -1)  # Get SF integer value from dictionary
            bw = self.BandWidth.get(bw_text, -1)  # Get BW integer value from dictionary
            cr = self.LoRaCR.get(cr_text, -1)  # Get CR integer value from dictionary
            file_code = self.file_size
            app_version = self.app_version_edit.value()
            return file_name, file_code, sf, bw, cr, app_version
        else:
            return None, None, None, None, None, None
    # Define a function to check if the filename contains special characters
    def contains_special_characters(self , filename):
        # Define a regular expression pattern to match special characters
        pattern = r'[!@#$%^&*(),.?":{}|<>]'
        # Use the search function to check if the pattern matches any part of the filename
        if re.search(pattern, filename):
            return True  # Special characters found
        else:
            return False  # No special characters found
        
#-------------------------------SIGN UP PAGE ------------------------------------------------
class SignupPage(QWidget):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Signup Page")
        self.setGeometry(200, 200, 300, 200)

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
    app.setStyle('Fusion')
    window = LoginPage()
    window.show()
    sys.exit(app.exec_())

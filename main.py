import sys
import zlib
from PyQt5.QtWidgets import *
from PyQt5.QtGui import *
from PyQt5.QtCore import *
import fb_connect_storage as firebase
import subprocess
import binascii
custom_crc_table = {}
poly = 0x04C11DB7
crc32_checksum = 0x00000000
class Worker(QObject):
    progress = pyqtSignal(int)
    finished = pyqtSignal()

    crc_calculated = pyqtSignal(str)  # New signal to emit the calculated CRC
    def __init__(self, file_path, firebase_instance):
        super().__init__()
        self.file_path = file_path
        self.firebase_instance = firebase_instance

    def process(self):
        self.firebase_instance.fb_add_file(self.file_path, self.file_path)
        self.generate_crc32_table(poly)
        crc32_checksum = self.crc32_stm(self.file_path)
        print(crc32_checksum)
        self.crc_calculated.emit(crc32_checksum)  # Emit the calculated CRC
        self.finished.emit()
    @staticmethod
    def generate_crc32_table(_poly):

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
        print(bytes_arr)
        length = len(bytes_arr)
        print(length)
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
    
    
class Firebase_Uploader(QWidget):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("File Uploader")
        self.setGeometry(200, 200, 500, 500)
        
        self.upload_button = QPushButton("Upload File", self)
        self.upload_button.setGeometry(150, 200, 200, 50)
        self.upload_button.clicked.connect(self.upload_file)

        self.font = QFont()
        self.font.setFamily('Arial')
        self.font.setPointSize(16)

        self.e1 = QLineEdit()
        self.e1.setValidator(QIntValidator())
        self.e1.setMaxLength(4)

        self.e2 = QLineEdit()
        self.e2.setValidator(QDoubleValidator(0.99 , 99.99 , 2))
        self.e2.setMaxLength(4)

        self.e3 = QLineEdit()
        self.e3.setValidator(QDoubleValidator(0.99 , 99.99 , 2))
        self.e3.setMaxLength(4)
        flo = QFormLayout()
        flo.addRow("CRC :", self.e1)
        flo.addRow("Version :", self.e2)
        flo.addRow("Date Release :" ,self.e3)

        self.setLayout(flo)
    
    def upload_file(self):
        file_dialog = QFileDialog(self)
        file_dialog.setNameFilter("Binary Files (*.bin)")
        file_dialog.setViewMode(QFileDialog.List)

        if file_dialog.exec_():
            file_paths = file_dialog.selectedFiles()
            
            if file_paths:
                file_path = file_paths[0]
                firebase_instance = firebase.FirebaseInit()

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
                worker = Worker(file_path, firebase_instance)
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

    
    def update_crc_field(self, crc_value):
        # Update the CRC field with the calculated value
        self.e1.setText(str(crc_value))


    def show_popup(self, title, message):
        msg = QMessageBox()
        msg.setWindowTitle(title)
        msg.setText(message)
        msg.exec_()


if __name__ == "__main__":
    app = QApplication(sys.argv)
    app.setStyle('Fusion')
    window = Firebase_Uploader()
    window.show()
    sys.exit(app.exec_())

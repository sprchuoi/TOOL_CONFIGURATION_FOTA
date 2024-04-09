import sys
import zlib
from PyQt5.QtWidgets import *
from PyQt5.QtGui import *
from PyQt5.QtCore import *
import fb_connect_storage as firebase

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

        flo = QFormLayout()
        flo.addRow("Integer validator:", self.e1)
        flo.addRow("Double validator:", self.e2)

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
                firebase_instance.fb_add_file(file_path)
                crc32_checksum = self.calculate_crc32(file_path)
                self.show_popup(f"CRC-32 Checksum for {file_path}:", str(crc32_checksum))
                self.show_popup(f"Upload success: {file_path}")

    def calculate_crc32(self, file_path):
        crc32_value = 0
        with open(file_path, 'rb') as f:
            for chunk in iter(lambda: f.read(4096), b''):
                crc32_value = zlib.crc32(chunk, crc32_value)
        return crc32_value

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

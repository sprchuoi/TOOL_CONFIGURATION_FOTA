import sys
from PyQt5.QtWidgets import QApplication, QMainWindow, QPushButton, QFileDialog


class FileUploader(QMainWindow):
    def __init__(self):
        super().__init__()

        self.setWindowTitle("File Uploader")

        self.upload_button = QPushButton("Upload File", self)
        self.upload_button.clicked.connect(self.upload_file)
        self.upload_button.setGeometry(50, 50, 200, 50)

        self.setGeometry(200, 200, 300, 150)

    def upload_file(self):
        file_dialog = QFileDialog(self)
        file_dialog.setNameFilter("Binary Files (*.bin)")
        file_dialog.setViewMode(QFileDialog.List)

        if file_dialog.exec_():
            file_paths = file_dialog.selectedFiles()
            if file_paths:
                print("Selected File Path:", file_paths[0])


if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = FileUploader()
    window.show()
    sys.exit(app.exec_())

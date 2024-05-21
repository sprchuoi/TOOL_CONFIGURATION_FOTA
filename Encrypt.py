from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import padding
from cryptography.hazmat.primitives.padding import PKCS7
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives import serialization
import os

class AES_Encrypt():
    def __init__(self, file_path, output_enfile):
        self.file_path = file_path
        self.output_enfile = output_enfile
    def encrypt_file(self):
        size_encrypt = 0
        key = bytes([0x2b, 0x7e, 0x15, 0x16, 0x28, 0xae, 0xd2, 0xa6, 0xab, 0xf7, 0x15, 0x88, 0x09, 0xcf, 0x4f, 0x3c])
        iv = bytes([0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f])
        cipher = Cipher(algorithms.AES(key), modes.CBC(iv), backend=default_backend())
        padder = padding.PKCS7(128).padder()
        with open(self.file_path, 'rb') as f_in, open(self.output_enfile, 'wb') as f_out:
            encryptor = cipher.encryptor()
            while True:
                block = f_in.read(128)  # Read 128 bytes (16 bytes for AES block size)
                if not block:
                    break
                padded_block = padder.update(block)
                ciphertext = encryptor.update(padded_block)
                f_out.write(ciphertext)
                size_encrypt += len(ciphertext)  # Update size with length of ciphertext
            # Finalize the padding and encryption
            padded_block = padder.finalize()
            ciphertext = encryptor.update(padded_block) + encryptor.finalize()
            f_out.write(ciphertext)
            size_encrypt += len(ciphertext)  # Update size with length of ciphertext
        return iv, size_encrypt
    def decrypt_file(self, iv):
        key = bytes([0x2b, 0x7e, 0x15, 0x16, 0x28, 0xae, 0xd2, 0xa6, 0xab, 0xf7, 0x15, 0x88, 0x09, 0xcf, 0x4f, 0x3c])
        cipher = Cipher(algorithms.AES(key), modes.CBC(iv), backend=default_backend())
        unpadder = padding.PKCS7(128).unpadder()

        with open(self.output_enfile, 'rb') as f_in, open(self.file_path + "_decrypted", 'wb') as f_out:
            decryptor = cipher.decryptor()
            while True:
                block = f_in.read(128)  # Read 128 bytes (16 bytes for AES block size)
                if not block:
                    break
                decrypted_block = decryptor.update(block)
                unpadded_block = unpadder.update(decrypted_block)
                f_out.write(unpadded_block)

            # Finalize the padding and decryption
            decrypted_block = decryptor.finalize()
            unpadded_block = unpadder.update(decrypted_block) + unpadder.finalize()
            f_out.write(unpadded_block)

        return self.file_path + "_decrypted"
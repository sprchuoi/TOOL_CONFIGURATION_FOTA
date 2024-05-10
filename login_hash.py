import hashlib
#sign up hash
def signup():
     email = input(“Enter email address: “)
     pwd = input(“Enter password: “)
     conf_pwd = input(“Confirm password: “)    
            if conf_pwd == pwd:
         enc = conf_pwd.encode()
         hash1 = hashlib.md5(enc).hexdigest()     with open(“credentials.txt”, “w”) as f:
         f.write(email + “\n”)
         f.write(hash1)
     f.close()
     print(“You have registered successfully!”)     else:
         print(“Password is not same as above! ”)
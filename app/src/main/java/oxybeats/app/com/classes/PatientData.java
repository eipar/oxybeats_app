package oxybeats.app.com.classes;

import android.os.Parcel;
import android.os.Parcelable;

public class PatientData implements Parcelable {
    private String User;
    private String Password;
    private String Name;
    private String Gender;
    private String Birth;


    public PatientData(){

    }

    public PatientData(String user, String password, String name, String Gender, String birth) {
        this.User = user;
        this.Password = password;
        this.Name = name;
        this.Gender = Gender;
        this.Birth = birth;
    }

    public PatientData(Parcel parcel){
        User = parcel.readString();
        Password = parcel.readString();
        Name = parcel.readString();
        Gender = parcel.readString();
        Birth = parcel.readString();
    }

    public static final Creator<PatientData> CREATOR = new Creator<PatientData>() {
        @Override
        public PatientData createFromParcel(Parcel in) {
            return new PatientData(in);
        }

        @Override
        public PatientData[] newArray(int size) {
            return new PatientData[size];
        }
    };

    public String getUser() {
        return User;
    }

    public String getPassword() {
        return Password;
    }

    public String getName() {
        return Name;
    }

    public String getGender() {
        return Gender;
    }

    public String getBirth() {
        return Birth;
    }

    public void setUser(String user) {
        User = user;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setGender(String Gender) {
        this.Gender = Gender;
    }

    public void setBirth(String birth) {
        Birth = birth;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(User);
        dest.writeString(Password);
        dest.writeString(Name);
        dest.writeString(Gender);
        dest.writeString(Birth);
    }
}

package pandelis.ga_testnavig;

/**
 * Created by PadPad on 6/9/2016.
 */
//setter and getter for the class LocationVar
public class LocationVar {

    //name and address string
    private Double Long,Lati,temp,humi,speed;
    private String key,id;

    public LocationVar() {
      /*Blank default constructor essential for Firebase*/
    }
    //Getters and setters

    public String getKey() {return key;}

    public void setkey(String key) {this.key = key;}

    public Double getLong() {
        return Long;
    }

    public void setLong(Double Long) {
        this.Long = Long;
    }

    public Double getLati() {
        return Lati;
    }

    public void setLati(Double Lati) {
        this.Lati = Lati;
    }


    public Double getTemp() {
        return temp;
    }

    public void setTemp(Double temp) {
        this.temp = temp;
    }


    public Double getHumi() {
        return humi;
    }

    public void setHumi(Double humi) {
        this.humi = humi;
    }

    public Double getSpeed() {return speed;}

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }




}
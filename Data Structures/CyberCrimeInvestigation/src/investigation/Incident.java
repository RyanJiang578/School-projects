package investigation;

public class Incident {

    private String operatingSys;
    private String webServer;
    private String date;
    private String urlHash; 
    private String ipAddressHash;
    private String geoLocation;

    public Incident(String operatingSys, String webServer, String date, String location, String ipHash, String urlHash){
        this.operatingSys = operatingSys;
        this.webServer = webServer;
        this.date = date;
        this.urlHash = urlHash;
        this.geoLocation = location;
        this.ipAddressHash = ipHash;
    }

    public String getOS() {
        return this.operatingSys;
    }

    public String getWebServer() {
        return this.webServer;
    }

    public String getURLHash() {
        return this.urlHash;
    }

    public String getDate() {
        return this.date;
    }

    public String getLocation() {
        return this.geoLocation;
    }

    public String getIPHash() {
        return this.ipAddressHash;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Incident i) {
            if (this.date.equals(i.getDate())) {
                if (this.geoLocation.equals(i.getLocation())) {
                    if (this.ipAddressHash.equals(i.getIPHash())) {
                        if (this.operatingSys.equals(i.getOS())) {
                            if (this.urlHash.equals(i.getURLHash())) {
                                if (this.webServer.equals(i.getWebServer())) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String toString(){
        return "{OS: " + this.operatingSys + ", " + "Web Server: " + this.webServer + ", "+ "URL Hash: " + this.urlHash+ ", IP Hash: " + this.ipAddressHash+ ", Location: " + this.geoLocation+"}";    
    }
}

package demography;

public class Default {

    Epoch epoch;
    Deme deme;

    public Default(){

    }

    public Default(Epoch epoch, Deme deme){
        this.epoch=epoch;
        this.deme=deme;
    }

    public Deme getDeme() {
        return deme;
    }

    public Epoch getEpoch() {
        return epoch;
    }

    public void setDeme(Deme deme) {
        this.deme = deme;
    }

    public void setEpoch(Epoch epoch) {
        this.epoch = epoch;
    }

    @Override
    public String toString() {
        return "Default{" + "epoch=" + epoch +
                ", deme=" + deme +
                '}';
    }
}

package ur.multithread.exec;

import java.util.Arrays;
import java.util.Date;

public class TaxiRecord {
    private int tolls_amount;
    private Date pickup_datetime;
    private double trip_distance;
    private double improvement_surcharge;
    private double mta_tax;
    private int rate_code_id;
    private Date dropoff_datetime;
    private int vendor_id;
    private double tip_amount;
    private double[] dropoff_location;
    private int passenger_count;
    private double[] pickup_location;
    private String store_and_fwd_flag;
    private int extra;
    private double total_amount;
    private int fare_amount;
    private int payment_type;

    public int getTolls_amount() {
        return tolls_amount;
    }

    public void setTolls_amount(int tolls_amount) {
        this.tolls_amount = tolls_amount;
    }

    public Date getPickup_datetime() {
        return pickup_datetime;
    }

    public void setPickup_datetime(Date pickup_datetime) {
        this.pickup_datetime = pickup_datetime;
    }

    public double getTrip_distance() {
        return trip_distance;
    }

    public void setTrip_distance(double trip_distance) {
        this.trip_distance = trip_distance;
    }

    public double getImprovement_surcharge() {
        return improvement_surcharge;
    }

    public void setImprovement_surcharge(double improvement_surcharge) {
        this.improvement_surcharge = improvement_surcharge;
    }

    public double getMta_tax() {
        return mta_tax;
    }

    public void setMta_tax(double mta_tax) {
        this.mta_tax = mta_tax;
    }

    public int getRate_code_id() {
        return rate_code_id;
    }

    public void setRate_code_id(int rate_code_id) {
        this.rate_code_id = rate_code_id;
    }

    public Date getDropoff_datetime() {
        return dropoff_datetime;
    }

    public void setDropoff_datetime(Date dropoff_datetime) {
        this.dropoff_datetime = dropoff_datetime;
    }

    public int getVendor_id() {
        return vendor_id;
    }

    public void setVendor_id(int vendor_id) {
        this.vendor_id = vendor_id;
    }

    public double getTip_amount() {
        return tip_amount;
    }

    public void setTip_amount(double tip_amount) {
        this.tip_amount = tip_amount;
    }

    public double[] getDropoff_location() {
        return dropoff_location;
    }

    public void setDropoff_location(double[] dropoff_location) {
        this.dropoff_location = dropoff_location;
    }

    public int getPassenger_count() {
        return passenger_count;
    }

    public void setPassenger_count(int passenger_count) {
        this.passenger_count = passenger_count;
    }

    public double[] getPickup_location() {
        return pickup_location;
    }

    public void setPickup_location(double[] pickup_location) {
        this.pickup_location = pickup_location;
    }

    public String getStore_and_fwd_flag() {
        return store_and_fwd_flag;
    }

    public void setStore_and_fwd_flag(String store_and_fwd_flag) {
        this.store_and_fwd_flag = store_and_fwd_flag;
    }

    public int getExtra() {
        return extra;
    }

    public void setExtra(int extra) {
        this.extra = extra;
    }

    public double getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(double total_amount) {
        this.total_amount = total_amount;
    }

    public int getFare_amount() {
        return fare_amount;
    }

    public void setFare_amount(int fare_amount) {
        this.fare_amount = fare_amount;
    }

    public int getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(int payment_type) {
        this.payment_type = payment_type;
    }

    @Override
    public String toString() {
        return "TaxiRecord{" +
                "tolls_amount=" + tolls_amount +
                ", pickup_datetime=" + pickup_datetime +
                ", trip_distance=" + trip_distance +
                ", improvement_surcharge=" + improvement_surcharge +
                ", mta_tax=" + mta_tax +
                ", rate_code_id=" + rate_code_id +
                ", dropoff_datetime=" + dropoff_datetime +
                ", vendor_id=" + vendor_id +
                ", tip_amount=" + tip_amount +
                ", dropoff_location=" + Arrays.toString(dropoff_location) +
                ", passenger_count=" + passenger_count +
                ", pickup_location=" + Arrays.toString(pickup_location) +
                ", store_and_fwd_flag='" + store_and_fwd_flag + '\'' +
                ", extra=" + extra +
                ", total_amount=" + total_amount +
                ", fare_amount=" + fare_amount +
                ", payment_type=" + payment_type +
                '}';
    }
}

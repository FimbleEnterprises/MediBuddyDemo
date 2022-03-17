package com.fimbleenterprises.medimileage.objects_and_containers;

import android.os.Parcel;
import android.os.Parcelable;

import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MyApp;
import com.fimbleenterprises.medimileage.R;

import java.util.ArrayList;

public class AggregatedSales implements Parcelable {

    public ArrayList<CrmEntities.OrderProducts.OrderProduct> allOrders;
    public float capitalTotal;
    public float probeTotal;
    public float flowmeterTotal;
    public float cableTotal;
    public float sparePartTotal;
    public float prvTotal;
    public float pafTotal;
    public float serviceMiscTotal;
    public float licenseTotal;
    public float leaseTotal;
    public float probeProductTotal;
    public float systemProductTotal;
    public float otherTotal;

    public int capitalCount;
    public int probeCount;
    public int flowmeterCount;
    public int cableCount;
    public int sparePartCount;
    public int prvCount;
    public int pafCount;
    public int serviceMiscCount;
    public int licenseCount;
    public int leaseCount;
    public int probeProductCount;
    public int systemProductCount;
    public int otherCount;
    
    
    public AggregatedSales(ArrayList<CrmEntities.OrderProducts.OrderProduct> orderProducts) {
        this.allOrders = orderProducts;
        build();
    }

    private void build() {

        for (CrmEntities.OrderProducts.OrderProduct product : allOrders) {

            if (product.isCapital) {
                capitalTotal += product.extendedAmt;
                capitalCount++;
            }

            if (product.partNumber.startsWith("PRV")) {
                prvTotal += product.extendedAmt;
                prvCount++;
            }

            if (product.partNumber.startsWith("PAF")) {
                pafTotal += product.extendedAmt;
                pafCount++;
            }

            switch (product.getFamily()) {
                case PROBE:
                    probeTotal += product.extendedAmt;
                    probeCount++;
                    break;
                case FLOWMETER:
                    flowmeterTotal += product.extendedAmt;
                    flowmeterCount++;
                    break;
                case AUX_CABLE:
                    cableTotal += product.extendedAmt;
                    cableCount++;
                    break;
                case SPARE_PART:
                    sparePartTotal += product.extendedAmt;
                    sparePartCount++;
                    break;
                case LICENSE_CARD:
                    licenseTotal += product.extendedAmt;
                    licenseCount++;
                    break;
                case LEASE_COMPLIANCE:
                    leaseTotal += product.extendedAmt;
                    leaseCount++;
                    break;
                case SERVICE_MISC:
                    serviceMiscTotal += product.extendedAmt;
                    serviceMiscCount++;
                    break;
                case PROBE_PRODUCT:
                    probeProductTotal += product.extendedAmt;
                    probeProductCount++;
                    break;
                case SYSTEM_PRODUCT:
                    systemProductTotal += product.extendedAmt;
                    systemProductCount++;
                    break;
                default:
                    otherTotal += product.extendedAmt;
            }
        }
        
    }

    public ArrayList<BasicObjects.BasicObject> toBasicObjects() {
        ArrayList<BasicObjects.BasicObject> objects = new ArrayList<>();

        if (capitalCount > 0) {
            objects.add(new BasicObjects.BasicObject(true,  MyApp.getAppContext()
                    .getString(R.string.capital_sales) + capitalCount
                        , Helpers.Numbers.convertToCurrency(capitalTotal)));
        }

        return objects;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.allOrders);
        dest.writeFloat(this.capitalTotal);
        dest.writeFloat(this.probeTotal);
        dest.writeFloat(this.flowmeterTotal);
        dest.writeFloat(this.cableTotal);
        dest.writeFloat(this.sparePartTotal);
        dest.writeFloat(this.prvTotal);
        dest.writeFloat(this.pafTotal);
        dest.writeFloat(this.serviceMiscTotal);
        dest.writeFloat(this.licenseTotal);
        dest.writeFloat(this.leaseTotal);
        dest.writeFloat(this.probeProductTotal);
        dest.writeFloat(this.systemProductTotal);
        dest.writeFloat(this.otherTotal);
        dest.writeInt(this.capitalCount);
        dest.writeInt(this.probeCount);
        dest.writeInt(this.flowmeterCount);
        dest.writeInt(this.cableCount);
        dest.writeInt(this.sparePartCount);
        dest.writeInt(this.prvCount);
        dest.writeInt(this.pafCount);
        dest.writeInt(this.serviceMiscCount);
        dest.writeInt(this.licenseCount);
        dest.writeInt(this.leaseCount);
        dest.writeInt(this.probeProductCount);
        dest.writeInt(this.systemProductCount);
        dest.writeInt(this.otherCount);
    }

    public void readFromParcel(Parcel source) {
        this.allOrders = source.createTypedArrayList(CrmEntities.OrderProducts.OrderProduct.CREATOR);
        this.capitalTotal = source.readFloat();
        this.probeTotal = source.readFloat();
        this.flowmeterTotal = source.readFloat();
        this.cableTotal = source.readFloat();
        this.sparePartTotal = source.readFloat();
        this.prvTotal = source.readFloat();
        this.pafTotal = source.readFloat();
        this.serviceMiscTotal = source.readFloat();
        this.licenseTotal = source.readFloat();
        this.leaseTotal = source.readFloat();
        this.probeProductTotal = source.readFloat();
        this.systemProductTotal = source.readFloat();
        this.otherTotal = source.readFloat();
        this.capitalCount = source.readInt();
        this.probeCount = source.readInt();
        this.flowmeterCount = source.readInt();
        this.cableCount = source.readInt();
        this.sparePartCount = source.readInt();
        this.prvCount = source.readInt();
        this.pafCount = source.readInt();
        this.serviceMiscCount = source.readInt();
        this.licenseCount = source.readInt();
        this.leaseCount = source.readInt();
        this.probeProductCount = source.readInt();
        this.systemProductCount = source.readInt();
        this.otherCount = source.readInt();
    }

    protected AggregatedSales(Parcel in) {
        this.allOrders = in.createTypedArrayList(CrmEntities.OrderProducts.OrderProduct.CREATOR);
        this.capitalTotal = in.readFloat();
        this.probeTotal = in.readFloat();
        this.flowmeterTotal = in.readFloat();
        this.cableTotal = in.readFloat();
        this.sparePartTotal = in.readFloat();
        this.prvTotal = in.readFloat();
        this.pafTotal = in.readFloat();
        this.serviceMiscTotal = in.readFloat();
        this.licenseTotal = in.readFloat();
        this.leaseTotal = in.readFloat();
        this.probeProductTotal = in.readFloat();
        this.systemProductTotal = in.readFloat();
        this.otherTotal = in.readFloat();
        this.capitalCount = in.readInt();
        this.probeCount = in.readInt();
        this.flowmeterCount = in.readInt();
        this.cableCount = in.readInt();
        this.sparePartCount = in.readInt();
        this.prvCount = in.readInt();
        this.pafCount = in.readInt();
        this.serviceMiscCount = in.readInt();
        this.licenseCount = in.readInt();
        this.leaseCount = in.readInt();
        this.probeProductCount = in.readInt();
        this.systemProductCount = in.readInt();
        this.otherCount = in.readInt();
    }

    public static final Parcelable.Creator<AggregatedSales> CREATOR = new Parcelable.Creator<AggregatedSales>() {
        @Override
        public AggregatedSales createFromParcel(Parcel source) {
            return new AggregatedSales(source);
        }

        @Override
        public AggregatedSales[] newArray(int size) {
            return new AggregatedSales[size];
        }
    };
}

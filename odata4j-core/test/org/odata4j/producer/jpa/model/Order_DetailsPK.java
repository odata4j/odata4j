package org.odata4j.producer.jpa.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Order_DetailsPK implements Serializable {
    @Basic(optional = false)
    @Column(name = "OrderID")
    private int orderID;
    @Basic(optional = false)
    @Column(name = "ProductID")
    private int productID;

    public Order_DetailsPK() {
    }

    public Order_DetailsPK(int orderID, int productID) {
        this.orderID = orderID;
        this.productID = productID;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) orderID;
        hash += (int) productID;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Order_DetailsPK)) {
            return false;
        }
        Order_DetailsPK other = (Order_DetailsPK) object;
        if (this.orderID != other.orderID) {
            return false;
        }
        if (this.productID != other.productID) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.odata4j.examples.producer.model.OrderDetailsPK[orderID=" + orderID + ", productID=" + productID + "]";
    }

}

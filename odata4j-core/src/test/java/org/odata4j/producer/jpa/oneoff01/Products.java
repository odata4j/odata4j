package org.odata4j.producer.jpa.oneoff01;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Products")
public class Products implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ProductID")
	private Integer ProductID;
    @Basic(optional = false)
    @Column(name = "ProductName")
    private String ProductName;
    @Column(name = "QuantityPerUnit")
    private String QuantityPerUnit;
    @Column(name = "UnitPrice")
	private BigDecimal UnitPrice;
    @Column(name = "UnitsInStock")
    private Short UnitsInStock;
    @Column(name = "UnitsOnOrder")
    private Short UnitsOnOrder;
    @Column(name = "ReorderLevel")
    private Short ReorderLevel;
    @Basic(optional = false)
    @Column(name = "Discontinued")
    private boolean Discontinued;
    
//    @JoinColumn(name = "CategoryID", referencedColumnName = "CategoryID")
//    @ManyToOne
//    private Categories01 Category;
    @Column(name = "CategoryID", insertable = false, updatable = false)
    private Integer CategoryID;
   

    public Products() {
    }

    public Products(Integer productID) {
        this.ProductID = productID;
    }

    public Products(Integer productID, String productName, boolean discontinued) {
        this.ProductID = productID;
        this.ProductName = productName;
        this.Discontinued = discontinued;
    }

    public Integer getProductID() {
        return ProductID;
    }

    public void setProductID(Integer productID) {
        this.ProductID = productID;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        this.ProductName = productName;
    }

    public String getQuantityPerUnit() {
        return QuantityPerUnit;
    }

    public void setQuantityPerUnit(String quantityPerUnit) {
        this.QuantityPerUnit = quantityPerUnit;
    }

    public BigDecimal getUnitPrice() {
        return UnitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.UnitPrice = unitPrice;
    }

    public Short getUnitsInStock() {
        return UnitsInStock;
    }

    public void setUnitsInStock(Short unitsInStock) {
        this.UnitsInStock = unitsInStock;
    }

    public Short getUnitsOnOrder() {
        return UnitsOnOrder;
    }

    public void setUnitsOnOrder(Short unitsOnOrder) {
        this.UnitsOnOrder = unitsOnOrder;
    }

    public Short getReorderLevel() {
        return ReorderLevel;
    }

    public void setReorderLevel(Short reorderLevel) {
        this.ReorderLevel = reorderLevel;
    }

    public boolean getDiscontinued() {
        return Discontinued;
    }

    public void setDiscontinued(boolean discontinued) {
        this.Discontinued = discontinued;
    }

//
//    public Categories01 getCategory() {
//        return Category;
//    }
//
//    public void setCategory(Categories01 categoryID) {
//        this.Category = categoryID;
//    }

    public void setCategoryID(Integer categoryID) {
        CategoryID = categoryID;
    }

    public Integer getCategoryID() {
        return CategoryID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (ProductID != null
                ? ProductID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Products)) {
            return false;
        }
        Products other = (Products) object;
        if ((this.ProductID == null && other.ProductID != null)
                || (this.ProductID != null && !this.ProductID
                        .equals(other.ProductID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.odata4j.examples.producer.model.Products[productID="
                + ProductID + "]";
    }
}

package org.jlinda.nest.dataio;

import org.esa.snap.datamodel.AbstractMetadata;
import org.esa.snap.datamodel.Unit;
import org.esa.snap.framework.datamodel.Band;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.gpf.Operator;
import org.esa.snap.framework.gpf.OperatorException;
import org.esa.snap.framework.gpf.OperatorSpi;
import org.esa.snap.framework.gpf.annotations.OperatorMetadata;
import org.esa.snap.framework.gpf.annotations.Parameter;
import org.esa.snap.framework.gpf.annotations.SourceProducts;
import org.esa.snap.framework.gpf.annotations.TargetProduct;
import org.esa.snap.gpf.OperatorUtils;
import org.esa.snap.util.ProductUtils;

@OperatorMetadata(alias = "SnaphuImport",
        category = "Radar/Interferometric/Unwrapping",
        description = "Ingest SNAPHU results into InSAR product.",
        internal = false)
public class SnaphuImportOp extends Operator {

    @SourceProducts(description = "The array of source product of InSAR bands.")
    private Product[] sourceProducts;

    private Product sourceProduct;

    @TargetProduct(description = "The target product for SNAPHU results.")
    private Product targetProduct;

    @Parameter(defaultValue="false", label="Do NOT save Wrapped interferogram in the target product")
    private boolean doNotKeepWrapped = false;

    @Override
    public void initialize() throws OperatorException {

        if (sourceProducts.length != 2) {
            throw new OperatorException("SnaphuImportOp requires EXACTLY two source products.");
        }

        sourceProduct = sourceProducts[0];

        try {

            final Product masterProduct;
            final Product slaveProduct;

            // check which one is the reference product:
            // ....check on geocodings, and pick 1st one that has them as 'master'...
            if (sourceProducts[0].getGeoCoding() != null && sourceProducts[0].getGeoCoding().canGetGeoPos()) {
                masterProduct = sourceProducts[0];
                slaveProduct = sourceProducts[1];
            } else if (sourceProducts[1].getGeoCoding() != null && sourceProducts[1].getGeoCoding().canGetGeoPos()) {
                masterProduct = sourceProducts[1];
                slaveProduct = sourceProducts[0];
            } else {
                throw new OperatorException("SnaphuImportOp requires at least one product with InSAR metadata.");
            }

            targetProduct = new Product(sourceProduct.getName(),
                    sourceProduct.getProductType(),
                    sourceProduct.getSceneRasterWidth(),
                    sourceProduct.getSceneRasterHeight());

            if (masterProduct.getSceneRasterHeight() != slaveProduct.getSceneRasterHeight()) {
                throw new OperatorException("SnaphuImportOp requires input products to be of the same HEIGHT dimension.");
            }

            if (masterProduct.getSceneRasterWidth() != slaveProduct.getSceneRasterWidth()) {
                throw new OperatorException("SnaphuImportOp requires input products to be of the same WIDTH dimension.");
            }

            // create target product
            // ....Note: the productType of target is of slaveProduct (it's about using the metadata of master,
            //           and bands of slave product)
            targetProduct = new Product(masterProduct.getName(),
                    slaveProduct.getProductType(),
                    masterProduct.getSceneRasterWidth(),
                    masterProduct.getSceneRasterHeight());

            ProductUtils.copyProductNodes(masterProduct, targetProduct);

            // add target bands to the target
            Band[] bands;

            if (!doNotKeepWrapped) {
                bands = masterProduct.getBands();
                for (Band srcBand : bands) {
                    String sourceBandName = srcBand.getName();
                    ProductUtils.copyBand(sourceBandName, masterProduct, sourceBandName, targetProduct, true);
                }
            }

            // assuming this is unwrapped phase result
            bands = slaveProduct.getBands();
            for (Band srcBand : bands) {

                final String masterDate = OperatorUtils.getAcquisitionDate(AbstractMetadata.getAbstractedMetadata(masterProduct));
                final String slaveDate = OperatorUtils.getAcquisitionDate(
                        masterProduct.getMetadataRoot().getElement(AbstractMetadata.SLAVE_METADATA_ROOT).getElements()[0]);

                String sourceBandName = srcBand.getName();
                final Band targetBand = ProductUtils.copyBand(sourceBandName, slaveProduct, sourceBandName, targetProduct, true);
                if (targetBand.getName().toLowerCase().contains("unw") || targetBand.getName().toLowerCase().contains("band")) {
                    targetBand.setUnit(Unit.ABS_PHASE); // if there is a band with "unw" set unit to ABS phase
                    targetBand.setName("Unw_Phase_ifg_" + masterDate + "_" + slaveDate); // set the name to Unw_Phase_ifg_masterDate_slaveDate
                }
            }

        } catch (Throwable e) {
            OperatorUtils.catchOperatorException(getId(), e);
        }

    }

    public static class Spi extends OperatorSpi {
        public Spi() {
            super(SnaphuImportOp.class);
        }
    }
}

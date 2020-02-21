package capstone.rt04.retailbackend.services;

import capstone.rt04.retailbackend.entities.*;
import capstone.rt04.retailbackend.repositories.*;
import capstone.rt04.retailbackend.util.enums.SizeEnum;
import capstone.rt04.retailbackend.util.enums.SortEnum;
import capstone.rt04.retailbackend.util.exceptions.InputDataValidationException;
import capstone.rt04.retailbackend.util.exceptions.category.CategoryNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.discount.DiscountNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.product.*;
import capstone.rt04.retailbackend.util.exceptions.promoCode.PromoCodeNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.store.StoreNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.style.StyleNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.tag.TagNotFoundException;
import capstone.rt04.retailbackend.util.exceptions.warehouse.WarehouseNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class ProductService {

    private final TagService tagService;
    private final CategoryService categoryService;
    private final DiscountService discountService;
    private final PromoCodeService promoCodeService;
    private final ValidationService validationService;
    private final StyleService styleService;
    private final StoreService storeService;
    private final WarehouseService warehouseService;
    private final SizeDetailsService sizeDetailsService;

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductStockRepository productStockRepository;
    private final ProductVariantRepository productVariantRepository;

    public ProductService(ValidationService validationService, TagService tagService, CategoryService categoryService, StyleService styleService,
                          StoreService storeService, ProductRepository productRepository, ProductVariantRepository productVariantRepository,
                          ProductStockRepository productStockRepository, ProductImageRepository productImageRepository, DiscountService discountService,
                          PromoCodeService promoCodeService, WarehouseService warehouseService, SizeDetailsService sizeDetailsService) {
        this.validationService = validationService;
        this.tagService = tagService;
        this.categoryService = categoryService;
        this.styleService = styleService;
        this.storeService = storeService;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.productStockRepository = productStockRepository;
        this.productImageRepository = productImageRepository;
        this.discountService = discountService;
        this.promoCodeService = promoCodeService;
        this.warehouseService = warehouseService;
        this.sizeDetailsService = sizeDetailsService;
    }

    public Product createNewProduct(Product product, Long categoryId, List<Long> tagIds, List<SizeEnum> sizes, List<String> colors) throws InputDataValidationException, CreateNewProductException, CategoryNotFoundException {

        if (categoryId == null) {
            throw new CreateNewProductException("The new product must be associated a leaf category");
        }

        // TODO: Generate product variant for each of the selected sizes and colours (also generate product stock for each of the generate product variant)

        Category category = categoryService.retrieveCategoryByCategoryId(categoryId);
        product.setCategory(category);

        Map<String, String> errorMap = validationService.generateErrorMap(product);

        if (errorMap == null) {
            try {
                if (!category.getChildCategories().isEmpty()) {
                    throw new CreateNewProductException("Selected category for the new product is not a leaf category");
                }

                product.setCategory(category);
                productRepository.save(product);

                category.getProducts().add(product);

                if (tagIds != null && (!tagIds.isEmpty())) {
                    for (Long tagId : tagIds) {
                        Tag tag = tagService.retrieveTagByTagId(tagId);
                        // addTag is implemented in the entity
                        product.addTag(tag);
                    }
                }

                ProductVariant productVariant;
                String sku;
                SizeDetails sizeDetails;

                for (String color : colors) {
                    for (SizeEnum size : sizes) {
                        sku = product.getSerialNumber() + "-" + color + "-" + size;
                        sizeDetails = new SizeDetails(size);
                        productVariant = new ProductVariant(sku, color, null, product, sizeDetailsService.createSizeDetails(sizeDetails));
                        createProductVariant(productVariant, product.getProductId());
                    }
                }

                return product;
            } catch (PersistenceException ex) {
                if (ex.getCause() != null
                        && ex.getCause().getCause() != null
                        && ex.getCause().getCause().getClass().getSimpleName().equals("SQLIntegrityConstraintViolationException")) {
                    throw new CreateNewProductException("Product with same SKU code already exist");
                } else {
                    throw new CreateNewProductException("An unexpected error has occurred: " + ex.getMessage());
                }
            } catch (Exception ex) {
                throw new CreateNewProductException("An unexpected error has occurred: " + ex.getMessage());
            }
        } else {
            throw new InputDataValidationException(errorMap, "Invalid data");
        }
    }

    public List<Product> retrieveAllProducts() {
        List<Product> products = productRepository.findAll();
        lazilyLoadProduct(products);
        return products;
    }

    public Product retrieveProductById(Long productId) throws ProductNotFoundException {
        if (productId == null) {
            throw new ProductNotFoundException("Product ID not provided");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product ID " + productId + " does not exist!"));

        List<Product> products = new ArrayList<>();
        products.add(product);
        lazilyLoadProduct(products);
        return product;
    }

    public List<Product> retrieveProductByCategory(Category category) {
        List<Product> products = productRepository.findAllByCategoryId(category.getCategoryId());
        lazilyLoadProduct(products);
        return products;
    }

    public List<Product> retrieveProductByCriteria(Category category, List<Tag> tags, List<String> colours, List<SizeDetails> sizeDetails,
                                                   BigDecimal minPrice, BigDecimal maxPrice, SortEnum sortEnum) {
        List<Product> products = new ArrayList<>();
        List<Product> productsByTag = null;

        if (tags == null || tags.size() == 0) {
            productsByTag = productRepository.findAllByCategoryId(category.getCategoryId());
        } else {
            productsByTag = productRepository.findAllByTagsIn(tags);
        }

        Boolean matchColour, matchSize, matchPriceRange;

        for (Product product : productsByTag) {
            matchSize = false;
            matchPriceRange = false;
            matchColour = false;

            if (product.getPrice().compareTo(minPrice) >= 0 && product.getPrice().compareTo(maxPrice) <= 0)
                matchPriceRange = true;

            for (ProductVariant productVariant : product.getProductVariants()) {
                if (colours != null && colours.size() > 0) {
                    if (colours.contains(productVariant.getColour())) {
                        matchColour = true;
                        if (sizeDetails != null && sizeDetails.size() > 0) {
                            for (SizeDetails sizeDetail : sizeDetails) {
                                if (sizeDetail.getProductSize().equals(productVariant.getSizeDetails().getProductSize())) {
                                    matchSize = true;
                                    break;
                                }
                            }
                        } else {
                            matchSize = true;
                        }
                    }
                } else {
                    matchColour = true;
                }
            }
            if (matchColour && matchSize && matchPriceRange) {
                products.add(product);
            }
        }

        if (sortEnum == SortEnum.PRICE_LOW_TO_HIGH) {
            Collections.sort(products, Comparator.comparing(Product::getPrice));
        } else if (sortEnum == SortEnum.PRICE_HIGH_TO_LOW) {
            Collections.sort(products, Comparator.comparing(Product::getPrice).reversed());
//            products.sort((Product product1, Product product2)->product2.getPrice().compareTo(product1.getPrice()));
//        } else if (sortEnum == SortEnum.RATING_HIGH_TO_LOW) {
//            // Find average
//        } else if (sortEnum == SortEnum.RATING_LOW_TO_HIGH) {
//            // Find average
        } else {
            Collections.sort(products, Comparator.comparing(Product::getProductId).reversed());
        }

        lazilyLoadProduct(products);
        return products;
    }

    public List<Product> retrieveListOfProductsById(List<Long> productIds) throws ProductNotFoundException {
        if (productIds == null) {
            throw new ProductNotFoundException("Product IDs not provided");
        }
        List<Product> products = (List<Product>) productRepository.findAllById(productIds);
        lazilyLoadProduct(products);
        return products;
    }

    private void lazilyLoadProduct(List<Product> products) {
        for (Product product : products) {
            product.getCategory();
            product.getTags().size();
            product.getReviews().size();
            product.getDiscounts().size();
            product.getPromoCodes().size();
            product.getProductVariants().size();
            product.getStyles().size();
        }
    }

    public Product updateProduct(Product newProduct) throws ProductNotFoundException {

        Product product = retrieveProductById(newProduct.getProductId());
        product.setPrice(newProduct.getPrice());
        product.setProductName(newProduct.getProductName());
        product.setCost(newProduct.getCost());
        product.setDescription(newProduct.getDescription());

//        product.setProductVariants(newProduct.getProductVariants()); -> createProductVariant / deleteProductVariant
//        product.setPromoCodes(newProduct.getPromoCodes()); -> add/remove promoCode
//        product.setCategory(newProduct.getCategory());
//        product.setTags(newProduct.getTags()); -> add/remove discount
//
        return product;
    }

    public Product deleteProduct(Long productId) throws ProductNotFoundException, ProductVariantNotFoundException, ProductStockNotFoundException//, DeleteProductException
    {
        Product productToRemove = retrieveProductById(productId);
        productToRemove.toString();
//        List<TransactionLineItem> saleTransactionLineItemEntities = saleTransactionEntityControllerLocal.retrieveSaleTransactionLineItemsByProductId(productId);
//        List<Review> reviewEntities = reviewEntityControllerLocal.retrieveReviewsForProduct(productId);
//        if (saleTransactionLineItemEntities.isEmpty() && reviewEntities.isEmpty()) {
        productToRemove.getCategory().getProducts().remove(productToRemove);

        for (Tag tag : productToRemove.getTags()) {
            tag.getProducts().remove(productToRemove);
        }
        productToRemove.getTags().clear();

        // TODO: Clear product variant
        List<ProductVariant> productVariants = new ArrayList<>(productToRemove.getProductVariants());
        for (ProductVariant productVariant : productVariants) {
            deleteProductVariant(productVariant.getProductVariantId());
//            productVariant.setProduct(null);
//            List<ProductStock> productStocks = productVariant.getProductStocks();
//            productVariant.setProductStocks(null);
//            productStocks.forEach(productStock -> {
//                productStock.setProductVariant(null);
//                productStockRepository.delete(productStock);
//            });
//            productVariantRepository.delete(productVariant);
            //TODO: have to search all shoppingcartItems and remove ref to productVariant
        }
        productRepository.delete(productToRemove);

        return productToRemove;
//        } else {
//            throw new DeleteProductException("Product ID " + productId + " is associated with existing sale transaction line item(s) and cannot be deleted!");
//        }
    }

    public void setProductPrice(Long productId, BigDecimal price) throws ProductNotFoundException {
        Product product = retrieveProductById(productId);
        product.setPrice(price);
    }

    public ProductVariant createProductVariant(ProductVariant productVariant, Long productId) throws ProductNotFoundException, InputDataValidationException, PersistenceException, CreateNewProductStockException, WarehouseNotFoundException, StoreNotFoundException {

        Product product = retrieveProductById(productId);
        productVariant.setProduct(product);
        productVariant.toString();

        Map<String, String> errorMap = validationService.generateErrorMap(productVariant);

        if (errorMap == null) {
            // TODO: Create ProductImage and link to Product before saving

            productVariantRepository.save(productVariant);
            product.getProductVariants().add(productVariant);

            // TODO: uncomment when warehouse and store services are done
            List<Warehouse> warehouses = warehouseService.retrieveAllWarehouses();
            List<Store> stores = storeService.retrieveAllStores();

            assignProductStock(warehouses, stores, productVariant);

            return productVariant;
        } else {
            throw new InputDataValidationException(errorMap, "Invalid Category");
        }
    }

    public ProductVariant retrieveProductVariantById(Long productVariantId) throws ProductVariantNotFoundException {
        if (productVariantId == null) {
            throw new ProductVariantNotFoundException("Product variant ID not provided");
        }

        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new ProductVariantNotFoundException("Product variant ID " + productVariantId + " does not exist!"));

        List<ProductVariant> productVariants = new ArrayList<>();
        productVariants.add(productVariant);
        lazilyLoadProductVariant(productVariants);

        return productVariant;
    }

    public ProductVariant retrieveProductVariantBySku(String sku) throws ProductVariantNotFoundException {
        if (sku == null) {
            throw new ProductVariantNotFoundException("Product variant SKU not provided");
        }

        ProductVariant productVariant = productVariantRepository.findBySKU(sku);

        if (productVariant == null) {
            throw new ProductVariantNotFoundException("Product variant with SKU " + sku + " does not exist!");
        }

        List<ProductVariant> productVariants = new ArrayList<ProductVariant>();
        productVariants.add(productVariant);
        lazilyLoadProductVariant(productVariants);
        return productVariant;
    }

    public List<ProductVariant> retrieveProductVariantByProduct(Long productId) {

        List<ProductVariant> productVariants = productVariantRepository.findAllByProduct_ProductId(productId);
        System.out.println(productVariants);
        lazilyLoadProductVariant(productVariants);
        return productVariants;
    }

    public List<ProductVariant> retrieveAllProductVariant() {
        List<ProductVariant> productVariants = (List<ProductVariant>) productVariantRepository.findAll();
        lazilyLoadProductVariant(productVariants);
        return productVariants;
    }


    private List<ProductVariant> lazilyLoadProductVariant(List<ProductVariant> productVariants) {
        for (ProductVariant productVariant : productVariants) {
            if (productVariant.getProductImages() != null) productVariant.getProductImages().size();
            productVariant.getProduct();
            productVariant.getSizeDetails();
            productVariant.getProductStocks().size();
        }
        return productVariants;
    }

    public ProductVariant updateProductVariant(ProductVariant newProductVariant) throws ProductVariantNotFoundException {
        ProductVariant productVariant = retrieveProductVariantById(newProductVariant.getProductVariantId());
        productVariant.setColour(newProductVariant.getColour());
        productVariant.setSKU(newProductVariant.getSKU());
        return productVariant;
    }

    public ProductVariant deleteProductVariant(Long productVariantId) throws ProductVariantNotFoundException, ProductStockNotFoundException {

        ProductVariant productVariant = retrieveProductVariantById(productVariantId);
        productVariant.toString();

        productVariant.setProductImages(null);
        productVariant.setSizeDetails(null);
        productVariant.getProduct().getProductVariants().remove(productVariant);
        productVariant.setProduct(null);

        List<ProductStock> productStocks = new ArrayList<>(productVariant.getProductStocks());

        for (ProductStock productStock : productStocks) {
            deleteProductStock(productStock.getProductStockId());
        }
        productVariantRepository.delete(productVariant);
        return productVariant;
    }

    // TODO : Call this method in createWarehouse / createStore

    /**
     * Scenarios:
     * 1. new product -> assign to List<Warehouse> and List<Store>
     * 2. new store -> retrieve List<Product> and assign to Store
     * 3. new warehouse -> retrieve List<Product> and assign to Warehouse
     *
     * @return void
     */
    public void assignProductStock(List<Warehouse> warehouses, List<Store> stores, ProductVariant inputProductVariant) throws CreateNewProductStockException, InputDataValidationException, WarehouseNotFoundException, StoreNotFoundException {
        List<ProductVariant> productVariants;
        if (inputProductVariant == null) {
            productVariants = retrieveAllProductVariant();
        } else {
            productVariants = new ArrayList<>();
            productVariants.add(inputProductVariant);
        }

        if (warehouses != null) {
            for (Warehouse w : warehouses) {
                Warehouse warehouse = warehouseService.retrieveWarehouseById(w.getWarehouseId());
                warehouseService.lazyLoadWarehouseFields(warehouse);
                for (ProductVariant productVariant : productVariants) {
                    ProductStock productStock = new ProductStock(0, 0, 0, 0);
                    productStock.setWarehouse(warehouse);
                    ProductStock newProductStock = createProductStock(productStock, productVariant.getProductVariantId());
                    warehouse.getProductStocks().add(newProductStock);
                }
            }
        }

        if (stores != null) {
            for (Store s : stores) {
                Store store = storeService.retrieveStoreById(s.getStoreId());

                for (ProductVariant productVariant : productVariants) {
                    ProductStock productStock = new ProductStock(0, 0, 0, 0);
                    productStock.setStore(store);
                    ProductStock newProductStock = createProductStock(productStock, productVariant.getProductVariantId());
                    store.getProductStocks().add(newProductStock);
                }
            }
        }
    }

    /**
     * Called in:
     * 1. createProductVariant
     * 2. createWarehouse / createStore ( to retrieve all productVariant and call createProductStock for each product variant
     *
     * @return productStock created
     */
    public ProductStock createProductStock(ProductStock productStock, Long productVariantId) throws InputDataValidationException, CreateNewProductStockException {
        Map<String, String> errorMap = validationService.generateErrorMap(productStock);
        if (errorMap == null) {
            try {
                ProductVariant productVariant = retrieveProductVariantById(productVariantId);
                productStock.setProductVariant(productVariant);
                productVariant.getProductStocks().add(productStock);
                productStockRepository.save(productStock);
                return productStock;
            } catch (Exception ex) {
                throw new CreateNewProductStockException("An unexpected error has occurred: " + ex.getMessage());
            }
        } else {
            throw new InputDataValidationException(errorMap, "Invalid input data");
        }
    }

    public ProductStock retrieveProductStockById(Long productStockId) throws ProductStockNotFoundException {
        ProductStock productStock = productStockRepository.findById(productStockId)
                .orElseThrow(() -> new ProductStockNotFoundException("Product stock ID " + productStockId + " does not exist!"));

        List<ProductStock> productStocks = new ArrayList<>();
        productStocks.add(productStock);
        lazilyLoadProductStock(productStocks);
        return productStock;
    }

    public List<ProductStock> retrieveProductStocksByParameter(Long storeId, Long warehouseId, Long productVariantId) {
        List<ProductStock> productStocks = new ArrayList<>();
        if (storeId != null) productStocks = productStockRepository.findAllByStoreStoreId(storeId);
        else if (warehouseId != null) productStocks = productStockRepository.findAllByWarehouseWarehouseId(warehouseId);
        else if (productVariantId != null)
            productStocks = productStockRepository.findAllByProductVariantProductVariantId(productVariantId);
        lazilyLoadProductStock(productStocks);
        return productStocks;
    }

    public List<ProductStock> retrieveAllProductStock() {
        List<ProductStock> productStocks = (List<ProductStock>) productStockRepository.findAll();
        lazilyLoadProductStock(productStocks);
        return productStocks;
    }

    private List<ProductStock> lazilyLoadProductStock(List<ProductStock> productStocks) {
        for (ProductStock productStock : productStocks) {
            productStock.getWarehouse();
            productStock.getStore();
            productStock.getProductVariant().getProductVariantId();
        }
        return productStocks;
    }

    public List<ProductStock> retrieveProductStockQuantityLessThanRequired(Long warehouseId) {
        List<ProductStock> productStocks = productStockRepository.findAllQuantityLessThanNotificationLevel(warehouseId);
        lazilyLoadProductStock(productStocks);
        return productStocks;
    }

    public ProductStock updateProductStock(ProductStock newProductStock) throws ProductStockNotFoundException {

        ProductStock productStock = retrieveProductStockById(newProductStock.getProductStockId());
        productStock.setQuantity(newProductStock.getQuantity());
        productStock.setMaxQuantity(newProductStock.getMaxQuantity());
        productStock.setNotificationLevel(newProductStock.getNotificationLevel());
        productStock.setQRcode(newProductStock.getQRcode());
        productStock.setReorderQuantity(newProductStock.getReorderQuantity());
        return productStock;
    }

    public ProductStock deleteProductStock(Long productStockId) throws ProductStockNotFoundException {
        ProductStock productStock = retrieveProductStockById(productStockId);
        productStock.toString();
        // TODO: Uncomment the codes when store and warehouse is done
        productStock.getProductVariant().getProductStocks().remove(productStock);
        productStock.setProductVariant(null);
        if (productStock.getStore() != null) productStock.getStore().getProductStocks().remove(productStock);
        productStock.setStore(null);
//        productStock.getWarehouse().getProductStocks().remove(productStock);
        productStock.setWarehouse(null);

        productStockRepository.delete(productStock);
        return productStock;
    }

    public List<ProductImage> createProductImage(List<ProductImage> productImages, Long productVariantId) throws ProductVariantNotFoundException {
        // Uploading to Google Drive will be done at frontend
        List<ProductImage> productImageList = retrieveProductVariantById(productVariantId).getProductImages();

        for (ProductImage productImage : productImages) {
            productImageRepository.save(productImage);
            productImageList.add(productImage);
        }
        return productImages;
    }

    public ProductImage retrieveProductImageById(Long productImageId) throws ProductImageNotFoundException {
        ProductImage productImage = productImageRepository.findById(productImageId)
                .orElseThrow(() -> new ProductImageNotFoundException("Product image " + productImageId + " not found!"));
        return productImage;
    }

    public List<ProductImage> retrieveProductImageByProductVariant(Long productVariantId) throws ProductImageNotFoundException, ProductVariantNotFoundException {
        ProductVariant productVariant = retrieveProductVariantById(productVariantId);
        List<Long> productImageIds = new ArrayList<>();
        for (ProductImage productImage : productVariant.getProductImages()) {
            productImageIds.add(productImage.getProductImageId());
        }
        List<ProductImage> productImages = productImageRepository.findAllByProductImageIdIn(productImageIds);
        return productImages;
    }

    public ProductImage updateProductImage(ProductImage newProductImage) throws ProductImageNotFoundException {
        ProductImage productImage = retrieveProductImageById(newProductImage.getProductImageId());
        productImage.setProductImageUrl(newProductImage.getProductImageUrl());
        return productImage;
    }

    public List<ProductImage> deleteProductImage(List<ProductImage> productImages, Long productVariantId) throws ProductImageNotFoundException, ProductVariantNotFoundException {
        ProductVariant productVariant = retrieveProductVariantById(productVariantId);

        List<ProductImage> deletedProductImages = new ArrayList<>();
        ProductImage productImage;

        for (ProductImage prodImage : productImages) {
            productImage = retrieveProductImageById(prodImage.getProductImageId());
            productVariant.getProductImages().remove(productImage);
            productImageRepository.delete(productImage);
            deletedProductImages.add(productImage);
        }
        return deletedProductImages;
    }

    /**
     * List of update operations for relationships of Product
     */
    public void changeCategoryForProduct(Long categoryId, Long productId) throws CategoryNotFoundException, ProductNotFoundException {
        Category newCategory = categoryService.retrieveCategoryByCategoryId(categoryId);
        Product product = retrieveProductById(productId);
        Category oldCategory = product.getCategory();

        oldCategory.getProducts().remove(product);
        product.setCategory(newCategory);
        newCategory.getProducts().add(product);
    }

    public void addOrRemoveTag(Long tagId, Long productId, List<Tag> tags, List<Product> products, Boolean isAppend) throws ProductNotFoundException, TagNotFoundException, TagNotFoundException, TagNotFoundException {
        // Adding / removing tag for a list of products
        if (tagId != null) {
            Tag tag = tagService.retrieveTagByTagId(tagId);
            Product product = null;

            for (Product prod : products) {
                product = retrieveProductById(prod.getProductId());
                if (isAppend) {
                    product.getTags().add(tag);
                    tag.getProducts().add(product);
                } else {
                    product.getTags().remove(tag);
                    tag.getProducts().remove(product);
                }
            }

        } else if (productId != null) {
            Product product = retrieveProductById(productId);
            Tag tag = null;

            for (Tag t : tags) {
                tag = tagService.retrieveTagByTagId(t.getTagId());
                if (isAppend) {
                    product.getTags().add(tag);
                    tag.getProducts().add(product);
                } else {
                    product.getTags().remove(tag);
                    tag.getProducts().remove(product);
                }
            }
        }
    }

    public void addOrRemovePromoCode(Long promoCodeId, Long productId, List<PromoCode> promoCodes, List<Product> products, Boolean isAppend) throws PromoCodeNotFoundException, ProductNotFoundException, PromoCodeNotFoundException, PromoCodeNotFoundException, PromoCodeNotFoundException {
        // Adding / removing promoCode for a list of products
        if (promoCodeId != null) {
            PromoCode promoCode = promoCodeService.retrievePromoCodeById(promoCodeId);
            Product product = null;

            for (Product prod : products) {
                product = retrieveProductById(prod.getProductId());
                if (isAppend) {
                    promoCode.getProducts().add(product);
                    product.getPromoCodes().add(promoCode);
                } else {
                    promoCode.getProducts().remove(product);
                    product.getPromoCodes().remove(promoCode);
                }
            }
        }
        // Adding / removing a list of promoCodes for a product
        else if (productId != null) {
            Product product = retrieveProductById(productId);
            PromoCode promoCode = null;

            for (PromoCode code : promoCodes) {
                promoCode = promoCodeService.retrievePromoCodeById(code.getPromoCodeId());
                if (isAppend) {
                    product.getPromoCodes().add(promoCode);
                    promoCode.getProducts().add(product);
                } else {
                    product.getPromoCodes().remove(promoCode);
                    promoCode.getProducts().remove(product);
                }
            }
        }
    }

    public void addOrRemoveDiscount(Long discountId, Long productId, List<Discount> discounts, List<Product> products, Boolean isAppend) throws ProductNotFoundException, DiscountNotFoundException, DiscountNotFoundException, DiscountNotFoundException, DiscountNotFoundException {
        // Adding / removing discount for a list of products
        if (discountId != null) {
            Discount discount = discountService.retrieveDiscountById(discountId);
            Product product = null;

            for (Product prod : products) {
                product = retrieveProductById(prod.getProductId());
                if (isAppend) {
                    discount.getProducts().add(product);
                    product.getDiscounts().add(discount);
                } else {
                    discount.getProducts().remove(product);
                    product.getDiscounts().remove(discount);
                }
            }
        }
        // Adding / removing a list of discounts for a product
        else if (productId != null) {
            Product product = retrieveProductById(productId);
            Discount discount = null;
            for (Discount disc : discounts) {
                discount = discountService.retrieveDiscountById(disc.getDiscountId());
                if (isAppend) {
                    product.getDiscounts().add(discount);
                    discount.getProducts().add(product);
                } else {
                    product.getDiscounts().remove(discount);
                    discount.getProducts().remove(product);
                }
            }
        }
    }

    public void addOrRemoveStyle(Long styleId, Long
            productId, List<Style> styles, List<Product> products, Boolean isAppend) throws
            ProductNotFoundException, TagNotFoundException, TagNotFoundException, TagNotFoundException, StyleNotFoundException, StyleNotFoundException {
        // Adding / removing style for a list of products
        if (styleId != null) {
            Style style = styleService.retrieveStyleByStyleId(styleId);
            Product product = null;

            for (Product prod : products) {
                product = retrieveProductById(prod.getProductId());
                if (isAppend) {
                    product.getStyles().add(style);
                    style.getProducts().add(product);
                } else {
                    product.getStyles().remove(style);
                    style.getProducts().remove(product);
                }
            }

        } else if (productId != null) {
            Product product = retrieveProductById(productId);
            Style style = null;

            for (Style sty : styles) {
                style = styleService.retrieveStyleByStyleId(sty.getStyleId());
                if (isAppend) {
                    product.getStyles().add(style);
                    style.getProducts().add(product);
                } else {
                    product.getStyles().remove(style);
                    style.getProducts().remove(product);
                }
            }
        }
    }
}
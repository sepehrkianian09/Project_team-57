package client.view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import server.controller.account.AdminControl;
import server.controller.account.CustomerControl;
import server.controller.account.VendorControl;
import server.controller.product.ProductControl;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import server.model.existence.Category;
import server.model.existence.Discount;
import server.model.existence.Off;
import server.model.existence.Product;
import notification.Notification;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static javafx.scene.control.ButtonType.NO;
import static javafx.scene.control.ButtonType.YES;

public class ProductsProcessor extends Processor{
    private static final int PRODUCT_SCROLL_PANE_WIDTH = 1050;
    private static final double PRODUCT_FIELD_WIDTH = 262.5;
    private static final int PRODUCT_FIELD_HEIGHT = 335;
    private static final int PRODUCT_PAGES_BAR_HEIGHT = 50;
    //UserProducts
    public BorderPane vendorProductsMainBorderPane;
    public Pane userProductsOptionPane;
    public JFXButton previousProductButton;
    public CheckBox approveProductCheckBox;
    public ImageView previousPageImageView;
    public JFXToggleButton offProductToggleButton;
    public ImageView inOffImage;
    public JFXButton removeFromCartButton;
    public StackPane productImageStackPane;
    public Pane purchaseButton;
    private HashMap<String, CheckBox> productsApprovalMap;

    public static enum ProductsMenuType {
        MAIN_PRODUCTS, VENDOR_PRODUCTS, CUSTOMER_CART, ADMIN_PRODUCT_REQUESTS,
        VENDOR_ADD_OFF_PRODUCTS, ADMIN_OFF_PRODUCTS, VENDOR_OFF_PRODUCTS,
        VENDOR_OFF_PRODUCTS_UNAPPROVED, PRODUCT_COMPARING_PRODUCTS;

    }
    private ProductsMenuType menuType;

    public ScrollPane productsScrollPane;
    //ProductPane
    public Rectangle productImage;
    public Label productNameLabel;
    public Label viewLabel;
    public ImageView availableImage;
    public Label availableLabel;
    public Label oldPriceLabel;
    public Label newPriceLabel;
    public Label pageNumberLabel;
    public ImageView nextPageButton;
    public ImageView previousPageButton;
    //Product Pane
    public JFXButton viewSortButton;
    public JFXButton timeSortButton;
    public JFXButton nameSortButton;
    public JFXButton scoreSortButton;
    public JFXToggleButton descendingSortButton;
    private JFXButton selectedSort;

    //Categories Pane
    public TreeTableView<Category> categoriesTableTreeView;

    public JFXTextField searchTextField;

    //Filter Pane
    public Pane mainFilterPane;
    public VBox filteredCategoriesVBox;
    public Label filterNameLabel;
    private Category filterCategory;
    private String filterName;

    //Filter Price Pane
    public JFXTextField toPriceTextField, fromPriceTextField;

    private ArrayList<Product> allProducts;
    private int rowSize = 4;
    private int columnMinSize = 2;
    private int columnMaxSize = 3;
    private int pageSize = columnMaxSize * rowSize;
    private int pageNumber = 0;
    private int productFieldsNumber;
    private int pageLim;
    private ProductControl productControl = ProductControl.getController();
    private Off selectedOff;

    //Discount Part
    public JFXToggleButton useDiscountCodeToggleButton;
    public JFXListView<Discount> discountCodesListView;
    private ListCell<Discount> selectedListCell;

    //Total Price Part
    public Label totalPriceLabel;
    public Label discountPriceLabel;
    public ImageView priceArrow;
    public Pane pricePane;

    public void initProcessor (ProductsMenuType menuType) {
        this.menuType = menuType;
        switch (menuType) {
            case MAIN_PRODUCTS:
                initMainProductsMenu();
                break;
            case VENDOR_PRODUCTS:
                initVendorProductsMenu();
                break;
            case CUSTOMER_CART:
                initProductsPage();
                initDiscountPart();
                initTotalPricePart();
                break;
            case ADMIN_PRODUCT_REQUESTS:
                initAdminProductRequestsMenu();
                break;
            case VENDOR_OFF_PRODUCTS_UNAPPROVED:
            case VENDOR_OFF_PRODUCTS:
            case ADMIN_OFF_PRODUCTS:
            case VENDOR_ADD_OFF_PRODUCTS:
                initOffProductsMenu();
                break;
            case PRODUCT_COMPARING_PRODUCTS:
                initProductsPage();
                break;
        }
    }

    private void initOffProductsMenu() {
        rowSize = 3;
        columnMinSize = 2;
        columnMaxSize = 3;
        pageSize = columnMaxSize * rowSize;
        initProductsPage();
    }

    private void initAdminProductRequestsMenu() {
        productsApprovalMap = new HashMap<>();
        initProductsPage();
    }

    private void initVendorProductsMenu() {
        Stop[] stops = new Stop[] {
                new Stop(0, Color.valueOf("#360033")),
                new Stop(1, Color.valueOf("#127183"))
        };
        LinearGradient linearGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        BackgroundFill backgroundFill = new BackgroundFill(linearGradient, CornerRadii.EMPTY, Insets.EMPTY);
        userProductsOptionPane.setBackground(new Background(backgroundFill));
        initProductsPage();
    }

    private void initMainProductsMenu() {
        productControl.initSort(); productControl.initFilter();
        selectedSort = viewSortButton;
        selectSort();
        initCategoriesTableTreeView();
        initPriceFiltersTextFields();
    }

    private void initCategoriesTableTreeView() {
        categoriesTableTreeView.setRowFactory( tv -> {
            TreeTableRow<Category> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 /*&& (!row.isEmpty())*/ ) {
                    Category category = row.getItem();
                    addCategoryToFilters(category);
                }
            });
            row.getStylesheets().add(getClass().getResource("FilterCategoryRowTable.css").toExternalForm());
            row.getStyleClass().add("Row");
            return row ;
        });

        categoriesTableTreeView.setRoot(ProductControl.getController().getCategoryTableRoot());
        categoriesTableTreeView.setShowRoot(false);
        //categoriesTableTreeView.getSelectionModel().selectFirst();
    }

    private void initPriceFiltersTextFields() {
        setPriceFields(toPriceTextField);
        setPriceFields(fromPriceTextField);
    }

    private void setPriceFields(JFXTextField priceTextField) {
        priceTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                //Todo Checking

                if(newValue.equals(".")) {
                    priceTextField.setText("0.");
                } else if (!newValue.matches("\\d+(.(\\d)+)?")) {
                    if(priceTextField.getText().contains(".")) {
                        priceTextField.setText(removeDots(priceTextField.getText()));
                    } else {
                        priceTextField.setText(newValue.replaceAll("[^\\d\\.]", ""));
                    }
                }

                setPriceFilter(/*priceTextField*/);
            }
        });
    }

    private void setPriceFilter(/*JFXTextField priceTextField*/) {
        double minPrice = fromPriceTextField.getText().isEmpty() ? 0 : Double.parseDouble(fromPriceTextField.getText());
        double maxPrice = toPriceTextField.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(toPriceTextField.getText());
        server.controller.Control.getController().setPriceFilters(minPrice, maxPrice);
        initProductsPage();
    }

    private void addCategoryToFilters(Category category) {
        if(!server.controller.Control.getController().isThereFilteringCategoryWithName(category.getName())) {
            try {
                loadFilterPane(category, null);
            } catch (IOException e) {
                //:)
            }
        }
    }

    private void loadFilterPane(Category filterCategory, String filterName) throws IOException {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FilterCategoryPane.fxml"));
            Node node = fxmlLoader.load();
            ProductsProcessor productsProcessor = fxmlLoader.getController();
            productsProcessor.parentProcessor = this;

            if (filterCategory == null) {
                productsProcessor.filterName = filterName;
                productsProcessor.filterNameLabel.setText(" " + filterName + " ");
                server.controller.Control.getController().addToFilterNameList(filterName);
                searchTextField.setText(null);
            } else if (filterName == null) {
                productsProcessor.filterCategory = filterCategory;
                productsProcessor.filterNameLabel.setText(" " + filterCategory.getName() + " ");
                server.controller.Control.getController().addToFilterCategoryList(filterCategory.getName());
            }

            initProductsPage();
            filteredCategoriesVBox.getChildren().add(node);
    }

    public void deleteFilterCategoryMouseClicked(MouseEvent mouseEvent) {
        if(filterCategory == null)
            server.controller.Control.getController().removeFromFilterNameList(filterName);
        else if(filterName == null)
            server.controller.Control.getController().removeFromFilterCategoryList(filterCategory.getName());

        ((ProductsProcessor)parentProcessor).initProductsPage();
        ((ProductsProcessor)parentProcessor).filteredCategoriesVBox.getChildren().remove(mainFilterPane);
    }

    public void showCartProducts(ActionEvent actionEvent) {
        if(server.controller.Control.getType() != null && !server.controller.Control.getType().equals("Customer")) {
            new Alert(Alert.AlertType.ERROR, "Sorry. Only Customers Can Use This Option Of The Market!").show();
            return;
        }
        if(canOpenSubStage(server.controller.Control.getUsername() + " Cart", this))
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("CustomerCartProducts.fxml"));
                Parent root = loader.load();
                ProductsProcessor processor = loader.getController();
                if(!(server.controller.Control.getType() != null && server.controller.Control.getType().equals("Customer")))
                    processor.userProductsOptionPane.getChildren().
                        removeAll(processor.discountCodesListView, processor.useDiscountCodeToggleButton);
                processor.parentProcessor = this;
                processor.initProcessor(ProductsProcessor.ProductsMenuType.CUSTOMER_CART);
                Stage stage = new Stage();
                stage.getIcons().add(new Image(IMAGE_FOLDER_URL + "Icons\\cart (2).png"));

                stage.setTitle((server.controller.Control.isLoggedIn() ? server.controller.Control.getUsername() : "Your")+ " Cart");
                stage.setScene(new Scene(root));
                stage.setResizable(false);
                this.subStages.add(stage);
                processor.setMyStage(stage);
                stage.getIcons().add(new Image(Main.class.getResourceAsStream("cartCustomer.png")));
                stage.show();
            } catch (IOException e) {
                //:)
            }
    }

    //initProductsScrollPane
    public void initProductsPage() {
        switch (menuType) {
            case MAIN_PRODUCTS:
                allProducts = productControl.getAllShowingProducts();
                break;
            case VENDOR_PRODUCTS:
                allProducts = VendorControl.getController().getAllProducts();
                break;
            case CUSTOMER_CART:
                if(server.controller.Control.getType() != null && server.controller.Control.getType().equals("Customer") && server.controller.Control.isLoggedIn())
                    allProducts = CustomerControl.getController().getAllCartProducts();
                else
                    allProducts = CustomerControl.getController().getTempCartProducts();
                purchaseButton.setDisable(allProducts.size() == 0);
                break;
            case ADMIN_PRODUCT_REQUESTS:
                allProducts = AdminControl.getController().getAllNotApprovedProducts();
                break;
            case VENDOR_ADD_OFF_PRODUCTS:
                allProducts = VendorControl.getController().getNonOffProducts();
                break;
            case VENDOR_OFF_PRODUCTS_UNAPPROVED:
            case ADMIN_OFF_PRODUCTS:
                if(!isThereOff(selectedOff))
                    return;
                allProducts = ProductControl.getController().getAllOffProductsByOffID(selectedOff);
                break;

            case VENDOR_OFF_PRODUCTS:
                if(!isThereOff(selectedOff))
                    return;
                allProducts = VendorControl.getController().getNonOffProducts(selectedOff.getOffID());
                break;
            case PRODUCT_COMPARING_PRODUCTS:
                allProducts = productControl.getAllComparingProducts();
                break;

        }
        initCertainProductsPage(productsScrollPane);
    }

    private boolean isThereOff(Off selectedOff) {
        if(productControl.isThereOffWithID(selectedOff.getOffID())) {
            selectedOff = productControl.getOffByID(selectedOff.getOffID());
            return true;
        }
        else {
            ((TableViewProcessor)((SaleProcessor)parentProcessor).parentProcessor).updateTable();
            ((TableViewProcessor)((SaleProcessor)parentProcessor).parentProcessor).updateSelectedItem();
            ((SaleProcessor)parentProcessor).myStage.close();
            return false;
        }
    }

    private void initCertainProductsPage(ScrollPane scrollPane) {
        try {
            BorderPane borderPane = new BorderPane();
            pageLim = (allProducts.size() - (pageNumber * pageSize) < pageSize ? (allProducts.size() -(pageNumber * pageSize)) : pageSize);
            productFieldsNumber = (pageLim <= (columnMinSize * rowSize) ? (columnMinSize * rowSize) : (columnMaxSize * rowSize));
            double borderPaneHeight = ((productFieldsNumber/rowSize) * PRODUCT_FIELD_HEIGHT) + PRODUCT_PAGES_BAR_HEIGHT;
            double borderPaneWidth = PRODUCT_FIELD_WIDTH * rowSize;
            borderPane.setPrefSize(borderPaneWidth - 50, borderPaneHeight);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            Pane root = setPageNumberBar();
            borderPane.setBottom(root);
            GridPane gridPane = new GridPane();
            gridPane.getChildren().addAll(getProductsPanes());
            gridPane.setMinWidth(Control.USE_COMPUTED_SIZE); gridPane.setMaxWidth(Control.USE_COMPUTED_SIZE); gridPane.setPrefWidth(Control.USE_COMPUTED_SIZE);
            gridPane.setMinHeight(Control.USE_COMPUTED_SIZE); gridPane.setMaxHeight(Control.USE_COMPUTED_SIZE); gridPane.setPrefHeight(Control.USE_COMPUTED_SIZE);
            borderPane.setCenter(gridPane);
            scrollPane.setContent(borderPane);
            scrollPane.setVvalue(0);
        } catch (IOException e) {
            //:)
        }
    }

    private Pane setPageNumberBar() throws IOException {
        FXMLLoader loader;
        if(rowSize == 4) {
            loader = new FXMLLoader(Main.class.getResource("ProductsPagesBar.fxml"));
        }  else {
            loader = new FXMLLoader(Main.class.getResource("ProductsPagesBarEnhancedEdition.fxml"));
        }
        Pane root = loader.load();
        ProductsProcessor pagesBarProcessor = loader.getController();
        pagesBarProcessor.setParentProcessor(this);
        if((int)Math.ceil(((double)allProducts.size())/pageSize) != 0)
            pagesBarProcessor.pageNumberLabel.setText("Page " + (pageNumber + 1) + " of " + (int)Math.ceil(((double)allProducts.size())/pageSize));
        else {
            pagesBarProcessor.pageNumberLabel.setText("Page " + (pageNumber + 1) + " of " + 1);
            pagesBarProcessor.nextPageButton.setDisable(true);
            pagesBarProcessor.nextPageButton.setOpacity(0.3);
            pagesBarProcessor.previousPageButton.setDisable(true);
            pagesBarProcessor.previousPageButton.setOpacity(0.3);
        }
        if(pageNumber == 0) {
            pagesBarProcessor.previousPageButton.setDisable(true);
            pagesBarProcessor.previousPageButton.setOpacity(0.3);
        }
        if(pageNumber == (((int)Math.ceil(((double)allProducts.size())/pageSize) - 1))) {
            pagesBarProcessor.nextPageButton.setDisable(true);
            pagesBarProcessor.nextPageButton.setOpacity(0.3);
        }
        return root;
    }

    private ArrayList<HBox> getProductsPanes() throws IOException {
        ArrayList<HBox> hBoxes = new ArrayList<>();
        for(int y = 0; y < productFieldsNumber/rowSize; ++y) {
            for(int x = 0; x < rowSize; ++ x) {
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER);
                GridPane.setConstraints(hBox, x, y);
                hBox.setMinWidth(255); hBox.setMaxWidth(255); hBox.setPrefWidth(255);
                hBox.setMinHeight(PRODUCT_FIELD_HEIGHT); hBox.setMaxHeight(PRODUCT_FIELD_HEIGHT); hBox.setPrefHeight(PRODUCT_FIELD_HEIGHT);
                hBoxes.add(hBox);
            }
        }
        if(menuType == ProductsMenuType.CUSTOMER_CART) {
            for(int i = 0; i < pageLim; ++i) {
                hBoxes.get(i).getChildren().add(getCustomerCartProductPane(i));
            }
        } else if(menuType == ProductsMenuType.ADMIN_PRODUCT_REQUESTS) {
            for(int i = 0; i < pageLim; ++i) {
                hBoxes.get(i).getChildren().add(getAdminProductRequestsProductPane(i));
            }

        } else if(menuType == ProductsMenuType.VENDOR_ADD_OFF_PRODUCTS || menuType == ProductsMenuType.VENDOR_OFF_PRODUCTS) {
            for(int i = 0; i < pageLim; ++i) {
                hBoxes.get(i).getChildren().add(getVendorOffProductPane(i));
            }
        } else {
            for(int i = 0; i < pageLim; ++i) {
                hBoxes.get(i).getChildren().add(getCommonProductPane(i));
            }
        }

        return hBoxes;
    }

    private Pane getCustomerCartProductPane(int productNumberInPage) throws IOException {
        Product product = allProducts.get(pageNumber * pageSize + productNumberInPage);
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("ProductPaneCustomerCart.fxml"));
        Pane productPane = loader.load();
        ProductsProcessor paneProcessor = loader.getController();
        paneProcessor.setParentProcessor(this);
        paneProcessor.removeFromCartButton.setOnAction(event -> {
            Optional<ButtonType> buttonType = new Alert(Alert.AlertType.CONFIRMATION, "Do You Really Want To Delete This Great Product From You Cart?", YES, NO).showAndWait();
            if(buttonType.get() == YES) {
                CustomerControl.getController().removeProductFromCartByID(product.getID()).getAlert().show();
                initProductsPage();
                initTotalPricePart();
            }
        });
        setProductStatusIcon(paneProcessor, product);
        setProductPaneImage(productPane, paneProcessor, product);
        paneProcessor.productNameLabel.setText(product.getName());
        setProductPanePrice(productPane, paneProcessor, product);
        setProductPaneOnMouseClick(productPane, product, this);
        return productPane;
    }

    private void setProductStatusIcon(ProductsProcessor paneProcessor, Product product) {
        if (!(product.getStatus() == 1 && (product.getCount() > 0 || product.getAmount() > 0))) {
            paneProcessor.availableImage.setImage(new Image(IMAGE_FOLDER_URL + "Icons\\ProductsMenu\\unavailable.png"));
            if(product.getStatus() != 1)
                paneProcessor.availableLabel.setText(product.getTheStatus());
            else
                paneProcessor.availableLabel.setText("Out Of Stock");
        }
    }

    private Pane getVendorOffProductPane(int productNumberInPage) throws IOException {
        Product product = allProducts.get(pageNumber * pageSize + productNumberInPage);
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("ProductPaneOff.fxml"));
        Pane productPane = loader.load();
        ProductsProcessor paneProcessor = loader.getController();
        paneProcessor.setParentProcessor(this);
        paneProcessor.offProductToggleButton.setOnAction(event -> {
            if(((JFXToggleButton)event.getSource()).isSelected()) {
                ((SaleProcessor)parentProcessor).addProductToOff(product.getID());
            } else {
                ((SaleProcessor)parentProcessor).deleteProductFromOff(product.getID());
            }
        });
        paneProcessor.offProductToggleButton.setSelected(((SaleProcessor)parentProcessor).isProductInOff(product.getID()));
        setProductStatusIcon(paneProcessor, product);
        setProductPaneImage(productPane, paneProcessor, product);
        paneProcessor.productNameLabel.setText(product.getName());
        setProductPanePrice(productPane, paneProcessor, product);
        setProductPaneOnMouseClick(productPane, product, this);
        return productPane;
    }

    private Pane getAdminProductRequestsProductPane(int productNumberInPage) throws IOException {
        Product product = allProducts.get(pageNumber * pageSize + productNumberInPage);
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("ProductPaneAdmin.fxml"));
        Pane productPane = loader.load();
        ProductsProcessor paneProcessor = loader.getController();
        paneProcessor.setParentProcessor(this);
        if(product.getStatus() == 3) {
            paneProcessor.previousProductButton.setOnAction(event -> {
                FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("ProductMenu.fxml"));
                try {
                    Parent root = fxmlLoader.load();
                    ProductProcessor processor = fxmlLoader.getController();
                    processor.setParentProcessor(paneProcessor.parentProcessor);
                    processor.setNonEdited(true);
                    processor.initProcessor(productControl.getProductById(product.getID()), ProductProcessor.ProductMenuType.ADMIN);
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setTitle(product.getName() + " Previous Menu");
                    processor.setMyStage(stage);
                    parentProcessor.addSubStage(stage);
                    stage.setResizable(false);
                    stage.getIcons().add(new Image(Main.class.getResourceAsStream("Product Icon.png")));
                    stage.show();
                } catch (IOException e) {
                    //:)
                }
            });
        } else {
            productPane.getChildren().remove(paneProcessor.previousProductButton);
            productPane.getChildren().remove(paneProcessor.previousPageImageView);
        }
        paneProcessor.approveProductCheckBox.setOnMouseClicked(event -> {
            if(productsApprovalMap.containsKey(product.getID())) {
                if(!paneProcessor.approveProductCheckBox.isSelected())
                    productsApprovalMap.remove(product.getID());
                else
                    productsApprovalMap.replace(product.getID(), paneProcessor.approveProductCheckBox);
            } else
                productsApprovalMap.put(product.getID(), paneProcessor.approveProductCheckBox);
        });
        paneProcessor.availableImage.setImage(new Image(IMAGE_FOLDER_URL + "Icons\\ProductsMenu\\unavailable.png"));
        paneProcessor.availableLabel.setText(product.getTheStatus());
        setProductPaneImage(productPane, paneProcessor, product);
        setProductPanePrice(productPane, paneProcessor, product);
        paneProcessor.productNameLabel.setText(product.getName());
        setProductPaneOnMouseClick(productPane, product, this);
        return productPane;
    }

    private Pane getCommonProductPane(int productNumberInPage) throws IOException {
        Product product = allProducts.get(pageNumber * pageSize + productNumberInPage);
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("ProductPane.fxml"));
        Pane productPane = loader.load();
        ProductsProcessor paneProcessor = loader.getController();
        setProductPaneImage(productPane, paneProcessor, product);
        paneProcessor.productNameLabel.setText(product.getName());
        setProductPanePrice(productPane, paneProcessor, product);
        if(product.getSeen() < 100)
            paneProcessor.viewLabel.setText("" + product.getSeen());
        else
            paneProcessor.viewLabel.setText("99+");
        setProductStatusIcon(paneProcessor, product);
        setProductPaneOnMouseClick(productPane, product, this);
        return productPane;
    }

    private void setProductPanePrice(Pane productPane, ProductsProcessor paneProcessor, Product product) {
        if (productControl.isThereProductInOff(product.getID())) {
            paneProcessor.oldPriceLabel.setText(getSmoothDoubleFormat(product.getPrice()) + "$");
            paneProcessor.oldPriceLabel.getStylesheets().addAll(Main.class.getResource(
                    "Strikethrough.css"
            ).toExternalForm());
            //paneProcessor.oldPriceLabel.setStyle("-fx-strikethrough: true;");
            paneProcessor.newPriceLabel.setText
                    (getSmoothDoubleFormat((product.getPrice() * (1 - (productControl.getOffByProductID(product.getID()).getOffPercent() / 100.0)))) + "$");
        } else {
            productPane.getChildren().removeAll(paneProcessor.newPriceLabel, paneProcessor.inOffImage);
            paneProcessor.oldPriceLabel.setText(getSmoothDoubleFormat(product.getPrice()) + "$");
        }
    }

    private void setProductPaneImage(Pane productPane, ProductsProcessor paneProcessor, Product product) {
        switch (menuType) {
            case VENDOR_PRODUCTS:
            case ADMIN_PRODUCT_REQUESTS:
            case VENDOR_ADD_OFF_PRODUCTS:
            case VENDOR_OFF_PRODUCTS:
            case VENDOR_OFF_PRODUCTS_UNAPPROVED:
                if(product.getStatus() != 3) {
                    paneProcessor.productImage.setFill(new ImagePattern(productControl.getProductImageByID(product.getID(), 1)));
                } else {
                    paneProcessor.productImage.setFill(new ImagePattern(productControl.getEditingProductImage(product.getID(), 1)));
                }
                break;
            default:
                paneProcessor.productImage.setFill(new ImagePattern(productControl.getProductImageByID(product.getID(), 1)));
                if(!(product.getCount() > 0 || product.getAmount() > 0)) {
                    ImageView imageView = new ImageView(new Image(IMAGE_FOLDER_URL + "Icons\\ProductsMenu\\sold out.png"));
                    imageView.setFitWidth(paneProcessor.productImage.getWidth());
                    imageView.setFitHeight(paneProcessor.productImage.getHeight());
                    imageView.setLayoutX(paneProcessor.productImageStackPane.getLayoutX()); imageView.setLayoutY(paneProcessor.productImageStackPane.getLayoutY());
                    productPane.getChildren().add(imageView);
                }
                break;
        }
    }

    private void setProductPaneOnMouseClick(Pane productPane, Product product, ProductsProcessor parentProcessor) {
        productPane.setOnMouseClicked(event -> {
            ProductProcessor.ProductMenuType productMenuType = null;
            switch (menuType) {
                case VENDOR_OFF_PRODUCTS:
                case VENDOR_ADD_OFF_PRODUCTS:
                case VENDOR_PRODUCTS:
                    if(product.getStatus() != 2)
                        productMenuType = ProductProcessor.ProductMenuType.VENDOR_EDIT;
                    else
                        productMenuType = ProductProcessor.ProductMenuType.VENDOR_EDIT_UNAPPROVED;
                    break;
                case ADMIN_OFF_PRODUCTS:
                case MAIN_PRODUCTS:
                    if(server.controller.Control.getType() != null && server.controller.Control.getType().equals("Admin")){
                        productMenuType = ProductProcessor.ProductMenuType.ADMIN;
                    } else if(server.controller.Control.getType() != null && server.controller.Control.getType().equals("Customer")) {
                        productMenuType = ProductProcessor.ProductMenuType.PRODUCTS_CUSTOMER;
                    } else if(server.controller.Control.getType() != null && server.controller.Control.getType().equals("Vendor"))
                        productMenuType = ProductProcessor.ProductMenuType.PRODUCTS_VENDOR;
                    else
                        productMenuType = ProductProcessor.ProductMenuType.PRODUCTS;
                    break;
                case CUSTOMER_CART:
                    productMenuType = ProductProcessor.ProductMenuType.CART;
                    break;
                case ADMIN_PRODUCT_REQUESTS:
                    productMenuType = ProductProcessor.ProductMenuType.ADMIN;
                    break;
                case VENDOR_OFF_PRODUCTS_UNAPPROVED:
                    return;
                case PRODUCT_COMPARING_PRODUCTS:
                    startComparing(product, parentProcessor);
                    return;
                //TODO(MORE)
            }
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("ProductMenu.fxml"));
            try {
                if(menuType == ProductsMenuType.MAIN_PRODUCTS) {
                    productControl.addSeenToProduct(product.getID());
                }
                Parent root = loader.load();
                ProductProcessor processor = loader.getController();
                processor.setParentProcessor(parentProcessor);
                processor.initProcessor(product, productMenuType);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle(product.getName() + " Menu");
                processor.setMyStage(stage);
                if(parentProcessor.parentProcessor != null)
                    parentProcessor.parentProcessor.addSubStage(stage);
                stage.setResizable(false);
                stage.getIcons().add(new Image(Main.class.getResourceAsStream("Product Icon.png")));
                stage.show();
            } catch (IOException e) {
                //:)
            }
        });
    }

    private void startComparing(Product product, ProductsProcessor parentProcessor) {
        try {
            productControl.setSecondComparingProduct(product.getID());
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("ComparingProductsMenu.fxml"));
            Parent root = loader.load();
            ProductProcessor processor = loader.getController();
            processor.initComparingProcessor(productControl.getComparingProducts()[0], productControl.getComparingProducts()[1]);
            myStage.setTitle("Compare " + productControl.getComparingProducts()[0].getName() + " And " + productControl.getComparingProducts()[1].getName());
            myStage.setScene(new Scene(root));
            processor.setMyStage(myStage);
            processor.setParentProcessor(parentProcessor.parentProcessor);
        } catch (IOException e) {
            //:)
        }
    }

    public void changePage(MouseEvent mouseEvent) {
        ImageView button = (ImageView) mouseEvent.getSource();
        if(button == nextPageButton) {
            ((ProductsProcessor)parentProcessor).pageNumber += 1;
        } else {
            ((ProductsProcessor)parentProcessor).pageNumber -= 1;
        }
        ((ProductsProcessor)parentProcessor).initProductsPage();
    }

    public void changePageOnMouse(MouseEvent mouseEvent) {
        String style = "-fx-opacity: 1; -fx-cursor: hand;";
        ((ImageView)mouseEvent.getSource()).setStyle(style);
    }

    public void changePageOutMouse(MouseEvent mouseEvent) {
        String style = "-fx-opacity: 0.7;";
        ((ImageView)mouseEvent.getSource()).setStyle(style);
    }

    public void sortButtonOnMouse(MouseEvent mouseEvent) {
        if ((JFXButton) mouseEvent.getSource() != selectedSort) {
            String style ="-fx-background-radius: 10 10 10 10; -fx-background-color: #90a4ae; -fx-cursor: hand;";
            ((JFXButton)mouseEvent.getSource()).setStyle(style);
        }
    }

    public void sortButtonOutMouse(MouseEvent mouseEvent) {
        if ((JFXButton) mouseEvent.getSource() != selectedSort) {
            String style ="-fx-background-radius: 10 10 10 10;";
            ((JFXButton)mouseEvent.getSource()).setStyle(style);
        }
    }

    public void changeSort(ActionEvent actionEvent) {
        String style ="-fx-background-radius: 10 10 10 10;";
        selectedSort.setStyle(style);
        selectedSort = (JFXButton) actionEvent.getSource();
        selectSort();
    }

    private void selectSort() {
        String style ="-fx-background-radius: 10 10 10 10; -fx-background-color: #607d8b;";
        selectedSort.setStyle(style);
        setSort();
    }

    public void setSort() {
        productControl.setSort(selectedSort.getText(), !descendingSortButton.isSelected());
        initProductsPage();
    }

    public void openAccountMenu(ActionEvent actionEvent) {
        new WelcomeProcessor().openAccountMenu();
    }

    public void filterByNameKeyTyped(KeyEvent keyEvent) {
        if((int) keyEvent.getCharacter().charAt(0) == 13) {
            filterByNameMouseClicked();
        }
    }

    public void filterByNameMouseClicked() {
        String searchFieldText = searchTextField.getText();

        if(searchFieldText == null || searchFieldText.isEmpty()) {
            searchTextField.requestFocus();
        } else if(!server.controller.Control.getController().isThereFilteringNameWithName(searchFieldText)) {
            try {
                loadFilterPane(null, searchTextField.getText());
            } catch (IOException e) {
                //:)
            }
        }

    }

    public void addNewProduct(MouseEvent mouseEvent) {
        if(canOpenSubStage("Add New Product", this)) {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("ProductMenu.fxml"));
            try {
                Parent root = loader.load();
                ProductProcessor processor = loader.getController();
                processor.initProcessor(new Product(), ProductProcessor.ProductMenuType.VENDOR_ADD);
                processor.setParentProcessor(this);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Add New Product");
                addSubStage(stage);
                processor.setMyStage(stage);
                stage.setResizable(false);
                stage.getIcons().add(new Image(Main.class.getResourceAsStream("Product Icon.png")));
                stage.show();
            } catch (IOException e) {
                //:)
            }
        }
    }

    public void addNewOff(MouseEvent mouseEvent) {
        if(canOpenSubStage("Add New Off", parentProcessor)) {
            try {
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("OffMenu.fxml"));
                Parent root = loader.load();
                SaleProcessor processor = loader.getController();
                processor.setParentProcessor(this.parentProcessor);
                processor.offInfoPaneMouseClick(null);
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.setTitle("Add New Off");
                newStage.setResizable(false);
                parentProcessor.addSubStage(newStage);
                processor.setMyStage(newStage);
                newStage.getIcons().add(new Image(Main.class.getResourceAsStream("Offs Icon.png")));
                newStage.show();
            } catch (IOException e) {
                //:);
            }
        }
    }

    public void saveChangesForAdminProductRequests(MouseEvent mouseEvent) {
        ArrayList<Notification> results = new ArrayList<>();
        AdminControl adminControl = AdminControl.getController();
        for (String productID : productsApprovalMap.keySet()) {
            if(productControl.getProductById(productID).getStatus() == 3) {
                results.add(adminControl.modifyEditingProductApprove(productID, productsApprovalMap.get(productID).isSelected()));
            } else {
                results.add(adminControl.modifyProductApprove(productID, productsApprovalMap.get(productID).isSelected()));
            }
        }
        showManageProductRequestsResult(results);
        initProductsPage();
    }

    private void showManageProductRequestsResult(ArrayList<Notification> results) {
        int errorCount = 0;
        int addProductAcceptCount = 0;
        int editProductAcceptCount = 0;
        int addProductDeclineCount = 0;
        int editProductDeclineCount = 0;
        for (Notification result : results) {
            switch (result) {
                case UNKNOWN_ERROR:
                    errorCount++;
                    break;
                case ACCEPT_EDITING_PRODUCT:
                    editProductAcceptCount++;
                    break;
                case ACCEPT_ADDING_PRODUCT:
                    addProductAcceptCount++;
                    break;
                case DECLINE_EDITING_PRODUCT:
                    editProductDeclineCount++;
                    break;
                case REMOVE_PRODUCT_SUCCESSFULLY:
                    addProductDeclineCount++;
                    break;
            }
        }
        new Alert(Alert.AlertType.INFORMATION, "Process Executed. Results:\n" +
                "Errors: " + errorCount + "\n" +
                "Accepted New Products: " + addProductAcceptCount +"\n" +
                "Accepted Edited Products: " + editProductAcceptCount +"\n" +
                "Declined New Products: " + addProductDeclineCount +"\n" +
                "Declined Edited Products: " + editProductDeclineCount +"\n" +
                "").show();
    }

    public void backToMainMenu(ActionEvent actionEvent) {
        Parent root = null;
        try {
            root = FXMLLoader.load(Main.class.getResource("WelcomeMenu.fxml"));
        } catch (IOException e) {
            //:)
        }
        Main.getStage().getIcons().remove(0);
        Main.getStage().getIcons().add(new Image(Main.class.getResourceAsStream("Main Icon.png")));
        Main.setScene("Boos Market", root);
    }

    public void setSelectedOff(Off selectedOff) {
        this.selectedOff = selectedOff;
    }

    //Purchase Part
    public void purchaseProducts(MouseEvent mouseEvent) {
        try {
            if(server.controller.Control.isLoggedIn() && server.controller.Control.getType() != null && server.controller.Control.getType().equals("Customer"))
                purchase();
            else
                openSignInMenu();
        } catch (IOException e) {
            //:)
        }

    }

    private void openSignInMenu() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SignInMenu.fxml"));
        Parent root = loader.load();
        WelcomeProcessor signInProcessor = loader.getController();
        signInProcessor.setParentProcessor(this);
        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        signInProcessor.setMyStage(newStage);
        newStage.getIcons().add(new Image(getClass().getResourceAsStream("Login Icon.png")));
        newStage.setResizable(false);
        newStage.setTitle("Sign In");
        newStage.show();
    }

    private void purchase() throws IOException {
        CustomerControl customerControl = CustomerControl.getController();
        if (customerControl.getAllCartProducts().size() == 0) {
            new Alert(Alert.AlertType.ERROR, "Your Cart Is Empty. At Some Products First Dude :/").show();
            return;
        }
        customerControl.setHasDiscount(selectedListCell != null);

        if (selectedListCell == null)
            customerControl.setDiscount(null);
        else
            customerControl.setDiscount(selectedListCell.getItem().getID());

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Purchase.fxml"));
        Parent root = fxmlLoader.load();
        CustomerProfileProcessor customerProfileProcessor = fxmlLoader.getController();
        customerProfileProcessor.parentProcessor = parentProcessor;
        customerProfileProcessor.setMyStage(myStage);
        myStage.setScene(new Scene(root));
        myStage.setTitle("Purchase Menu");
    }

    //Discount Part
    private void initDiscountPart() {
        setDiscountsListViewSpecifications();
        useDiscountCodeToggleButton.selectedProperty().addListener((observable, oldValue, newValue) -> discountToggleButtonChanged());
    }

    //Todo Check
    private void setDiscountsListViewSpecifications() {
        discountCodesListView.setCellFactory(param -> {
            ListCell<Discount> listCell = new ListCell<Discount>() {
                @Override
                protected void updateItem(Discount discount, boolean empty) {
                    super.updateItem(discount, empty);

                    if(!empty)
                        setText(discount.getCode() + " : " + discount.getDiscountPercent() + "%");
                }
            };
            listCell.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && event.getButton().equals(MouseButton.PRIMARY) && listCell.getItem() != null) {
                    if(selectedListCell != null)
                        selectedListCell.setStyle("");
                    listCell.setStyle("-fx-background-color: #10A0DC;");
                    selectedListCell = listCell;
                    showDiscountEffect(listCell.getItem());
                }
            });
            listCell.getStyleClass().add("Discounts_ListView_Cell");
            return listCell;
        });
    }

    private void discountToggleButtonChanged() {
        if(useDiscountCodeToggleButton.isSelected()) {
            setDiscountListViewCells();
        } else {
            removeDiscountListViewCells();
            removeDiscountEffect();
        }
    }

    private void setDiscountListViewCells() {
        //Todo Check
        discountCodesListView.setDisable(false);
        ObservableList<Discount> customerDiscounts = discountCodesListView.getItems();
        customerDiscounts.addAll(CustomerControl.getController().getAllAvailableCustomerDisCounts());
    }

    private void removeDiscountListViewCells() {
        discountCodesListView.getItems().removeAll(discountCodesListView.getItems());
        discountCodesListView.setDisable(true);

        if(selectedListCell != null) {
            selectedListCell.setStyle("");
            selectedListCell = null;
        }
    }

    private void showDiscountEffect(Discount discount) {
        if(totalPriceLabel.getPrefWidth() == 157) {
            setPriceWithDiscountLabel(calculatePriceWithDiscount(discount.getDiscountPercent()));
            setDiscountPriceArrow();
        } else {
            discountPriceLabel.setText(calculatePriceWithDiscount(discount.getDiscountPercent()) + " $");
        }
    }

    private void removeDiscountEffect() {
        if(totalPriceLabel.getPrefWidth() != 157) {
            pricePane.getChildren().removeAll(discountPriceLabel, priceArrow);
            totalPriceLabel.setPrefWidth(157.0);
        }
    }

    private void setDiscountPriceArrow() {
        try {
            FileInputStream imageFileInputStream = new FileInputStream("src\\main\\resources\\Images\\Icons\\ProductsMenu\\Arrow Cart.png");
            ImageView priceArrow = new ImageView(new Image(imageFileInputStream));
            priceArrow.setFitWidth(20.0);
            priceArrow.setFitHeight(20.0);

            priceArrow.setLayoutX(totalPriceLabel.getLayoutX() + totalPriceLabel.getPrefWidth() + 5.0);
            priceArrow.setLayoutY(totalPriceLabel.getLayoutY());

            pricePane.getChildren().add(priceArrow);
            this.priceArrow = priceArrow;
            imageFileInputStream.close();
        } catch (IOException e) {
            //:)
        }
    }

    private void setPriceWithDiscountLabel(double discountPrice) {
        Label discountPriceLabel = new Label(getSmoothDoubleFormat(discountPrice) + " $");

        discountPriceLabel.setPrefHeight(totalPriceLabel.getPrefHeight());
        discountPriceLabel.setPrefWidth((totalPriceLabel.getPrefWidth() - 30.0) / 2);
        totalPriceLabel.setPrefWidth(discountPriceLabel.getPrefWidth());

        discountPriceLabel.setLayoutY(totalPriceLabel.getLayoutY());
        discountPriceLabel.setLayoutX(totalPriceLabel.getLayoutX() + totalPriceLabel.getPrefWidth() + 30.0);
        pricePane.getChildren().add(discountPriceLabel);
        this.discountPriceLabel = discountPriceLabel;
    }

    private double calculatePriceWithDiscount(double discountPercent) {
        return (1 - discountPercent / 100) * Double.parseDouble(totalPriceLabel.getText().replace(" $", ""));
    }

    //Price Part
    protected void initTotalPricePart() {
        double totalPrice = CustomerControl.getController().getTotalPriceWithoutDiscount();
        totalPriceLabel.setText(getSmoothDoubleFormat(totalPrice));
    }


}
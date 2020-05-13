package view;

import model.existence.Account;
import model.existence.Category;
import model.existence.Discount;
import model.existence.Product;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public interface PrintOptionSpecs {
    default void printOptionSpecs(Object option) {
        if(option instanceof Account) {
            printAccountSpecs((Account) option);
        } else if(option instanceof Product) {
            printProductSpecs((Product) option);
        } else if(option instanceof Category) {
            printCategorySpecs((Category) option);
        } else if(option instanceof String) {
            printFilteringOptionSpecs((String) option);
        } else if(option instanceof Discount) {
            printDiscountSpecs((Discount) option);
        }
    }

    default void printAccountSpecs(Account account){
        this.printCustomLineForAccount();

        printWithNullCheckingForAccount("UserName", account.getUsername());
        this.printCustomLineForAccount();

        printWithNullCheckingForAccount("PassWord", "************");
        this.printCustomLineForAccount();

        printWithNullCheckingForAccount("Account Type", account.getType());
        this.printCustomLineForAccount();

        printWithNullCheckingForAccount("FirstName", account.getFirstName());
        this.printCustomLineForAccount();

        printWithNullCheckingForAccount("LastName", account.getLastName());
        this.printCustomLineForAccount();

        printWithNullCheckingForAccount("Email", account.getEmail());
        this.printCustomLineForAccount();

        if(account.getType().equals("Vendor")){
            printWithNullCheckingForAccount("Brand", account.getBrand());
            this.printCustomLineForAccount();
        }
        if(!account.getType().equals("Admin"))
        {
            System.out.format("| %-15s | %-35f | %n", "Credit", account.getCredit());
            this.printCustomLineForAccount();
        }
    }

    default void printWithNullCheckingForAccount(String fieldName, String fieldValue){
        if(fieldValue == null)
            System.out.format("| %-15s | %-35s | %n", fieldName, "Not Assigned");
        else
            System.out.format("| %-15s | %-35s | %n", fieldName, fieldValue);
    }

    default void printCustomLineForAccount(){
        System.out.println("+-----------------+-------------------------------------+");
    }

    default void printProductSpecs(Product product){
        this.printCustomLineForProduct();

        printCustomStatus(product.getStatus());
        this.printCustomLineForProduct();

        printWithNullCheckingForProduct("Name", product.getName());
        this.printCustomLineForProduct();

        printWithNullCheckingForProduct("Brand Name", product.getBrand());
        this.printCustomLineForProduct();

        printWithNullCheckingForProduct("Seller Name", product.getSellerUserName());
        this.printCustomLineForProduct();

        printCustomCount(product);
        this.printCustomLineForProduct();

        printWithNullCheckingForProduct("Category", product.getCategory());
        this.printCustomLineForProduct();

        printCustomDescriptionForProduct(product.getDescription());
        this.printCustomLineForProduct();

        System.out.format("| %-20s | %-35f | %n", "Price", product.getPrice());
        this.printCustomLineForProduct();

        System.out.format("| %-20s | %-35d | %n", "View", product.getSeen());
        this.printCustomLineForProduct();

        System.out.format("| %-20s | %-35f | %n", "Average Score", product.getAverageScore());
        this.printCustomLineForProduct();
    }

    default void printCustomStatus(int status){
        String state = null;

        if(status == 1){
            state = "Approved";
        } else if(status == 2){
            state = "Waiting For Creating Approval";
        } else if(status == 3){
            state = "Waiting For Editing Approval";
        }

        System.out.format("| %-20s | %-35s | %n", "Status", state);
    }

    default void printCustomCount(Product product){
        if(product.isCountable()){
            System.out.format("| %-20s | %-35d | %n", "Count", product.getCount());
        } else {
            System.out.format("| %-20s | %-35f | %n", "Amount", product.getAmount());
        }
    }

    default void printCustomDescriptionForProduct(String description){
        if(description != null) {
            ArrayList<String> splitDescription = splitDescriptionForProduct(description);

            for(int i = 0; i < splitDescription.size(); i++){
                if(i == 0)
                    System.out.format("| %-20s | %-35s | %n", "Description", splitDescription.get(i));
                else
                    System.out.format("| %-20s | %-35s | %n", "", splitDescription.get(i));
            }
        } else {
            System.out.format("| %-20s | %-35s | %n", "Description", "Not Assigned");
        }
    }

    default ArrayList<String> splitDescriptionForProduct(String description) {
        ArrayList<String> splitDescription = new ArrayList<>();

        if(description.length() > 35) {
            Character[] splitCharacters = new Character[] {' ', '\t', '.', ',', '!', '\n'};
            ArrayList<Character> characters = new ArrayList<>(Arrays.asList(splitCharacters));

            int splitIndex = 0;

            for(int i = 34; i > 0; i--) {
                if(characters.contains(description.charAt(i))) {
                    splitIndex = i;
                    break;
                }
            }

            splitDescription.add(description.substring(0, splitIndex + 1));
            splitDescription.addAll(splitDescriptionForProduct(description.substring(splitIndex + 1)));
        } else {
            splitDescription.add(description);
        }

        return splitDescription;
    }

    default void printCustomLineForProduct(){
        System.out.println("+----------------------+-------------------------------------+");
    }

    default void printWithNullCheckingForProduct(String fieldName, String fieldValue){
        if(fieldValue == null)
            System.out.format("| %-20s | %-35s | %n", fieldName, "Not Assigned");
        else
            System.out.format("| %-20s | %-35s | %n", fieldName, fieldValue);
    }

    default void printCategorySpecs(Category category) {
        printCustomLineForCategory();

        printWithNullCheckingForCategory("Name", category.getName());
        printCustomLineForCategory();

        printWithNullCheckingForCategory("ParentName", category.getParentCategory());
        printCustomLineForCategory();

        printCustomFeaturesForCategory(category.getFeatures());
        printCustomLineForCategory();
    }

    default void printCustomLineForCategory() {
        System.out.println("+----------------------+-------------------------------------+");
    }

    default void printWithNullCheckingForCategory(String fieldName, String fieldValue) {
        if(fieldValue == null)
            System.out.format("| %-20s | %-35s | %n", fieldName, "Not Assigned");
        else
            System.out.format("| %-20s | %-35s | %n", fieldName, fieldValue);
    }

    default void printCustomFeaturesForCategory(String features) {
        if(features != null) {
            ArrayList<String> splitFeatures = splitFeaturesForCategory(features);

            for(int i = 0; i < splitFeatures.size(); i++){
                if(i == 0)
                    System.out.format("| %-20s | %-35s | %n", "Features", splitFeatures.get(i));
                else
                    System.out.format("| %-20s | %-35s | %n", "", splitFeatures.get(i));
            }
        } else {
            System.out.format("| %-20s | %-35s | %n", "Features", "Not Assigned");
        }
    }

    default ArrayList<String> splitFeaturesForCategory(String features) {
        ArrayList<String> splitFeatures = new ArrayList<>();

        if(features.length() > 35) {
            Character[] splitCharacters = new Character[] {' ', '\t', '.', ',', '!', '\n'};
            ArrayList<Character> characters = new ArrayList<>(Arrays.asList(splitCharacters));

            int splitIndex = 0;

            for(int i = 34; i > 0; i--) {
                if(characters.contains(features.charAt(i))) {
                    splitIndex = i;
                    break;
                }
            }

            splitFeatures.add(features.substring(0, splitIndex + 1));
            splitFeatures.addAll(splitFeaturesForCategory(features.substring(splitIndex + 1)));
        } else {
            splitFeatures.add(features);
        }

        return splitFeatures;
    }

    default void printFilteringOptionSpecs(String filteringOption) {
        //TODO
        System.out.println("********    " + filteringOption + "    ********");
    }

    default void printDiscountSpecs(Discount discount) {
        printCustomLineForDiscount();

        printWithNullCheckingForDiscount("Discount Code", discount.getCode());
        printCustomLineForDiscount();

        printCustomDateForDiscount("Start Date", discount.getStartDate());
        printCustomLineForDiscount();

        printCustomDateForDiscount("Finish Date", discount.getFinishDate());
        printCustomLineForDiscount();

        System.out.format("| %-22s | %-35f |", "Discount Percent", discount.getDiscountPercent());
        printCustomLineForDiscount();

        System.out.format("| %-22s | %-35f |", "Maximum Discount", discount.getMaxDiscount());
        printCustomLineForDiscount();

        System.out.format("| %-22s | %-35d |", "Maximum Repetition", discount.getMaxRepetition());
        printCustomLineForDiscount();

        printCustomersListForDiscount(discount.getCustomersWithRepetition());
        printCustomLineForDiscount();

    }

    default void printCustomLineForDiscount() {
        System.out.println("+------------------------+-------------------------------------+");
    }

    default void printWithNullCheckingForDiscount(String fieldName, String fieldValue) {
        if(fieldValue == null)
            System.out.format("| %-22s | %-35s | %n", fieldName, "Not Assigned");
        else
            System.out.format("| %-22s | %-35s | %n", fieldName, fieldValue);
    }

    default void printCustomDateForDiscount(String fieldName, Date date) {
        if(date == null) {
            System.out.format("| %-22s | %-35s | %n", fieldName, "Not Assigned");
        } else {
            java.util.Date date1 = new java.util.Date(date.getTime());
            System.out.format("| %-22s | %-35s | %n", fieldName, date1.toString());
        }
    }

    default void printCustomersListForDiscount(HashMap<String, Integer> customersHashMap) {
        if(customersHashMap == null) {
            System.out.format("| %-22s | %-35s |", "Customers", "Not Assigned");
        } else {
            HashMap<String, Integer> clonedCustomersHashMap = (HashMap<String, Integer>) customersHashMap.clone();
            ArrayList<String> splitCustomers = splitCustomers(clonedCustomersHashMap);

            for (String customerLine : splitCustomers) {
                if (customerLine.equals(splitCustomers.get(0))) {
                    System.out.format("| %-22s | %-35s |", "Customers", customerLine);
                } else
                    System.out.format("| %-22s | %-35s |", "", customerLine);
            }
        }
    }

    default ArrayList<String> splitCustomers(HashMap<String, Integer> customersHashMap) {
        ArrayList<String> splitCustomers = new ArrayList<>();
        StringBuilder customersLine = new StringBuilder("");
        boolean flag = true;

        for(String customer : customersHashMap.keySet()) {
            if(customersLine.length() + customer.length() > 33) {
                break;
            }
            customersLine.insert(customersLine.length() - 1, customer + ", ");
            customersHashMap.remove(customer);
        }

        splitCustomers.add(customersLine.toString());
        splitCustomers.addAll(splitCustomers(customersHashMap));
        return splitCustomers;
    }
}

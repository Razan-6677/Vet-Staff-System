import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;

public class JavaApplication extends Application {
    private Stage primaryStage;
    private ObservableList<Animal> animals = FXCollections.observableArrayList();
    private ObservableList<Owner> owners = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Veterinary Clinic System");
        loadDataFromFiles();
        showLoginScreen();
    }

    private void loadDataFromFiles() {
        animals.clear();
        owners.clear();

        try {

            File ownerFile = new File("owners.txt");
            if (ownerFile.exists()) {
                try (Scanner ownerScanner = new Scanner(ownerFile)) {
                    while (ownerScanner.hasNextLine()) {
                        String[] parts = ownerScanner.nextLine().split(",");
                        if (parts.length >= 3) {
                            owners.add(new Owner(parts[0], parts[1], parts[2]));
                        }
                    }
                }
            }


            File animalFile = new File("animals.txt");
            if (animalFile.exists()) {
                try (Scanner animalScanner = new Scanner(animalFile)) {
                    while (animalScanner.hasNextLine()) {
                        String[] parts = animalScanner.nextLine().split(",");
                        if (parts.length >= 4) {
                            switch (parts[0]) {
                                case "Dog" -> animals.add(new Dog(parts[1], Integer.parseInt(parts[2]), parts[3]));
                                case "Cat" -> animals.add(new Cat(parts[1], Integer.parseInt(parts[2]), Boolean.parseBoolean(parts[3])));
                                case "Bird" -> animals.add(new Bird(parts[1], Integer.parseInt(parts[2]), Boolean.parseBoolean(parts[3])));
                            }
                        }
                    }
                }
            }


            File relationFile = new File("relations.txt");
            if (relationFile.exists()) {
                try (Scanner relationScanner = new Scanner(relationFile)) {
                    while (relationScanner.hasNextLine()) {
                        String[] parts = relationScanner.nextLine().split(",");
                        if (parts.length >= 2) {
                            Owner owner = findOwnerByName(parts[0]);
                            Animal pet = findAnimalByName(parts[1]);
                            if (owner != null && pet != null) {
                                owner.addPet(pet);
                            }
                        }
                    }
                }
            }

            if (owners.isEmpty() && animals.isEmpty()) {
                initializeSampleData();
            }
        } catch (FileNotFoundException e) {
            System.out.println("No saved data found, starting with sample data");
            initializeSampleData();
        } catch (NumberFormatException e) {
            showAlert("Error", "Failed to load data: " + e.getMessage());
            initializeSampleData();
        }
    }

    private void initializeSampleData() {
        animals.add(new Dog("Buddy", 3, "Golden Retriever"));
        animals.add(new Cat("Miso", 2, true));
        animals.add(new Bird("Twitter", 1, false));
        
        Owner owner1 = new Owner("John", "1", "0501111111");
        Owner owner2 = new Owner("Sarah", "2", "0502222222");
        
        owner1.addPet(animals.get(0)); 
        owner2.addPet(animals.get(1)); 
        owner2.addPet(animals.get(2)); 
        
        owners.addAll(owner1, owner2);
    }

    private void showLoginScreen() {
        ImageView logoView = createLogo();
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        Label statusLabel = new Label();

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 1, 2);
        grid.add(statusLabel, 1, 3);

        loginButton.setOnAction(e -> {
            if (authenticate(usernameField.getText(), passwordField.getText())) {
                showMainScreen();
            } else {
                statusLabel.setText("Invalid credentials!");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        if (logoView != null) layout.getChildren().add(logoView);
        layout.getChildren().add(grid);

        Scene scene = new Scene(layout, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showMainScreen() {
        TabPane tabPane = new TabPane();
        

        Tab animalsTab = new Tab("Animals", createAnimalsTab());
        animalsTab.setClosable(false);
        

        Tab ownersTab = new Tab("Owners", createOwnersTab());
        ownersTab.setClosable(false);
        
        tabPane.getTabs().addAll(animalsTab, ownersTab);
        
        Button saveBtn = new Button("Save Data");
        saveBtn.setOnAction(e -> saveDataToFiles());
        
        VBox layout = new VBox(20, tabPane, saveBtn);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(layout, 900, 600);
        primaryStage.setScene(scene);
    }

    private VBox createAnimalsTab() {
        TableView<Animal> animalsTable = createAnimalsTable();
        HBox buttonsBox = createAnimalButtons();
        return new VBox(20, animalsTable, buttonsBox);
    }

    private TableView<Animal> createAnimalsTable() {
        TableView<Animal> table = new TableView<>();
        
        TableColumn<Animal, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Animal, Integer> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        
        TableColumn<Animal, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        TableColumn<Animal, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
        
        TableColumn<Animal, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(cellData -> {
            Owner owner = findOwnerByPet(cellData.getValue());
            return new SimpleStringProperty(owner != null ? owner.getName() : "None");
        });

        table.setItems(animals);
        table.getColumns().addAll(nameCol, ageCol, typeCol, detailsCol, ownerCol);
        return table;
    }

    private HBox createAnimalButtons() {
        Button addDogBtn = new Button("Add Dog");
        addDogBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addDogBtn.setOnAction(e -> showAddAnimalDialog("Dog"));
        
        Button addCatBtn = new Button("Add Cat");
        addCatBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addCatBtn.setOnAction(e -> showAddAnimalDialog("Cat"));
        
        Button addBirdBtn = new Button("Add Bird");
        addBirdBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addBirdBtn.setOnAction(e -> showAddAnimalDialog("Bird"));
        
        Button assignOwnerBtn = new Button("Assign Owner");
        assignOwnerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        assignOwnerBtn.setOnAction(e -> assignOwnerToAnimal());
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> deleteSelectedAnimal());
        
        return new HBox(10, addDogBtn, addCatBtn, addBirdBtn, assignOwnerBtn, deleteBtn);
    }

    private VBox createOwnersTab() {
        TableView<Owner> ownersTable = new TableView<>();
        ownersTable.setItems(owners);
        
        TableColumn<Owner, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Owner, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Owner, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        
        TableColumn<Owner, Integer> petsCountCol = new TableColumn<>("Pets Count");
        petsCountCol.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getPets().size()).asObject());
        
        ownersTable.getColumns().addAll(nameCol, idCol, phoneCol, petsCountCol);
        
        TableView<Animal> petsTable = new TableView<>();
        
        TableColumn<Animal, String> petNameCol = new TableColumn<>("Pet Name");
        petNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Animal, String> petTypeCol = new TableColumn<>("Pet Type");
        petTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        petsTable.getColumns().addAll(petNameCol, petTypeCol);
        
        ownersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                petsTable.setItems(newVal.getPets());
            } else {
                petsTable.setItems(FXCollections.emptyObservableList());
            }
        });
        
        Button addOwnerBtn = new Button("Add Owner");
        addOwnerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addOwnerBtn.setOnAction(e -> showAddOwnerDialog());
        
        Button deleteOwnerBtn = new Button("Delete Owner");
        deleteOwnerBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        deleteOwnerBtn.setOnAction(e -> {
            deleteSelectedOwner();
            ownersTable.refresh();
            petsTable.setItems(FXCollections.emptyObservableList());
        });
        
        VBox layout = new VBox(20);
        layout.getChildren().addAll(
            new Label("Owners List"),
            ownersTable,
            new Label("Selected Owner's Pets"),
            petsTable,
            new HBox(10, addOwnerBtn, deleteOwnerBtn)
        );
        
        return layout;
    }

    private void showAddAnimalDialog(String animalType) {
        Dialog<Animal> dialog = new Dialog<>();
        dialog.setTitle("Add " + animalType);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField ageField = new TextField();
        ageField.setPromptText("Age");
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Age:"), 0, 1);
        grid.add(ageField, 1, 1);
        
        ComboBox<Owner> ownerCombo = new ComboBox<>(owners);
        ownerCombo.setPromptText("Select Owner");
        grid.add(new Label("Owner:"), 0, 2);
        grid.add(ownerCombo, 1, 2);
        
        switch (animalType) {
            case "Dog" -> {
                TextField breedField = new TextField();
                breedField.setPromptText("Breed");
                grid.add(new Label("Breed:"), 0, 3);
                grid.add(breedField, 1, 3);
            }
            case "Cat" -> {
                CheckBox indoorCheck = new CheckBox("Indoor Cat");
                grid.add(new Label("Type:"), 0, 3);
                grid.add(indoorCheck, 1, 3);
            }
            case "Bird" -> {
                CheckBox canFlyCheck = new CheckBox("Can Fly");
                grid.add(new Label("Ability:"), 0, 3);
                grid.add(canFlyCheck, 1, 3);
            }
        }
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter((ButtonType buttonType) -> {
            if (buttonType == ButtonType.OK) {
                try {
                    Animal animal = null;
                    String name = nameField.getText();
                    int age = Integer.parseInt(ageField.getText());
                    
                    switch (animalType) {
                        case "Dog" -> animal = new Dog(name, age, ((TextField)grid.getChildren().get(7)).getText());
                        case "Cat" -> animal = new Cat(name, age, ((CheckBox)grid.getChildren().get(7)).isSelected());
                        case "Bird" -> animal = new Bird(name, age, ((CheckBox)grid.getChildren().get(7)).isSelected());
                    }
                    
                    Owner selectedOwner = ownerCombo.getValue();
                    if (selectedOwner != null && animal != null) {
                        selectedOwner.addPet(animal);
                    }
                    return animal;
                } catch (NumberFormatException e) {
                    showAlert("Error", "Please enter a valid age");
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(animal -> {
            if (animal != null) {
                animals.add(animal);
                showAlert("Success", "Animal added successfully");
            }
        });
    }

    private void showAddOwnerDialog() {
        Dialog<Owner> dialog = new Dialog<>();
        dialog.setTitle("Add New Owner");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField idField = new TextField();
        idField.setPromptText("ID");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("ID:"), 0, 1);
        grid.add(idField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new Owner(
                    nameField.getText(),
                    idField.getText(),
                    phoneField.getText()
                );
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(owner -> {
            owners.add(owner);
            showAlert("Success", "Owner added successfully");
        });
    }

    private void assignOwnerToAnimal() {
        Animal selectedAnimal = getSelectedAnimal();
        if (selectedAnimal == null) {
            showAlert("Warning", "Please select an animal first");
            return;
        }

        Dialog<Owner> dialog = new Dialog<>();
        dialog.setTitle("Assign Owner to Animal");
        
        ComboBox<Owner> ownerCombo = new ComboBox<>(owners);
        ownerCombo.setPromptText("Select Owner");
        
        Owner currentOwner = findOwnerByPet(selectedAnimal);
        if (currentOwner != null) {
            currentOwner.removePet(selectedAnimal);
        }
        
        dialog.getDialogPane().setContent(new VBox(10, 
            new Label("Animal: " + selectedAnimal.getName()),
            new Label("Select Owner:"),
            ownerCombo
        ));
        
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return ownerCombo.getValue();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(owner -> {
            owner.addPet(selectedAnimal);
            showAlert("Success", "Animal assigned to owner: " + owner.getName());
            refreshTables();
        });
    }

    private void deleteSelectedAnimal() {
        Animal selected = getSelectedAnimal();
        if (selected != null) {
            Owner owner = findOwnerByPet(selected);
            if (owner != null) {
                owner.removePet(selected);
            }
            animals.remove(selected);
            showAlert("Success", "Animal deleted successfully");
        } else {
            showAlert("Warning", "Please select an animal to delete");
        }
    }

    private void deleteSelectedOwner() {
        Owner selected = getSelectedOwner();
        if (selected != null) {
            
            List<Animal> petsToRemove = new ArrayList<>(selected.getPets());
            
            for (Animal pet : petsToRemove) {
                animals.remove(pet);
            }
            

            boolean removed = owners.remove(selected);
            
            if (removed) {

                refreshAllTables();
                showAlert("Success", "Owner and associated pets deleted successfully");
            } else {
                showAlert("Error", "Failed to delete owner");
            }
        } else {
            showAlert("Warning", "Please select an owner to delete");
        }
    }

    private void refreshAllTables() {
        TabPane tabPane = (TabPane) primaryStage.getScene().getRoot().getChildrenUnmodifiable().get(0);
        
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getContent() instanceof VBox vBox) {
                for (javafx.scene.Node node : vBox.getChildren()) {
                    if (node instanceof TableView) {
                        TableView<?> tableView = (TableView<?>) node;
                        

                        tableView.setItems(null);
                        tableView.layout();
                        
                        if (tab.getText().equals("Owners")) {
                            ((TableView<Owner>) tableView).setItems(owners);
                        } else if (tab.getText().equals("Animals")) {
                            ((TableView<Animal>) tableView).setItems(animals);
                        }
                        
                        tableView.refresh();
                    }
                }
            }
        }
    }

    private Animal getSelectedAnimal() {
        TabPane tabPane = (TabPane) primaryStage.getScene().getRoot().getChildrenUnmodifiable().get(0);
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        
        if (selectedTab != null && selectedTab.getText().equals("Animals")) {
            VBox tabContent = (VBox) selectedTab.getContent();
            if (tabContent.getChildren().get(0) instanceof TableView) {
                @SuppressWarnings("unchecked")
                TableView<Animal> table = (TableView<Animal>) tabContent.getChildren().get(0);
                return table.getSelectionModel().getSelectedItem();
            }
        }
        return null;
    }

    private Owner getSelectedOwner() {
        TabPane tabPane = (TabPane) primaryStage.getScene().getRoot().getChildrenUnmodifiable().get(0);
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        
        if (selectedTab != null && selectedTab.getText().equals("Owners")) {
            VBox tabContent = (VBox) selectedTab.getContent();
            if (tabContent.getChildren().get(1) instanceof TableView) {
                @SuppressWarnings("unchecked")
                TableView<Owner> table = (TableView<Owner>) tabContent.getChildren().get(1);
                return table.getSelectionModel().getSelectedItem();
            }
        }
        return null;
    }

    private Owner findOwnerByPet(Animal animal) {
        for (Owner owner : owners) {
            if (owner.getPets().contains(animal)) {
                return owner;
            }
        }
        return null;
    }

    private Owner findOwnerByName(String name) {
        for (Owner owner : owners) {
            if (owner.getName().equals(name)) {
                return owner;
            }
        }
        return null;
    }

    private Animal findAnimalByName(String name) {
        for (Animal animal : animals) {
            if (animal.getName().equals(name)) {
                return animal;
            }
        }
        return null;
    }

    private void refreshTables() {
        TabPane tabPane = (TabPane) primaryStage.getScene().getRoot().getChildrenUnmodifiable().get(0);
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getContent() instanceof VBox vBox) {
                for (var node : vBox.getChildren()) {
                    if (node instanceof TableView) {
                        ((TableView<?>) node).refresh();
                    }
                }
            }
        }
    }

    private void saveDataToFiles() {
        try {
            // Save animals
            try (PrintWriter animalWriter = new PrintWriter("animals.txt")) {
                for (Animal animal : animals) {
                    switch (animal) {
                        case Dog dog -> animalWriter.println("Dog," + dog.getName() + "," + dog.getAge() + "," + dog.getBreed());
                        case Cat cat -> animalWriter.println("Cat," + cat.getName() + "," + cat.getAge() + "," + cat.isIndoor());
                        case Bird bird -> animalWriter.println("Bird," + bird.getName() + "," + bird.getAge() + "," + bird.isCanFly());
                        default -> {
                        }
                    }
                }
            }

            // Save owners
            try (PrintWriter ownerWriter = new PrintWriter("owners.txt")) {
                for (Owner owner : owners) {
                    ownerWriter.println(owner.getName() + "," + owner.getId() + "," + owner.getPhoneNumber());
                }
            }

            // Save relationships
            try (PrintWriter relationWriter = new PrintWriter("relations.txt")) {
                for (Owner owner : owners) {
                    for (Animal pet : owner.getPets()) {
                        relationWriter.println(owner.getName() + "," + pet.getName());
                    }
                }
            }

            showAlert("Success", "Data saved successfully");
        } catch (IOException e) {
            showAlert("Error", "Failed to save data: " + e.getMessage());
        }
    }

    private ImageView createLogo() {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/veterinary.png"));
            ImageView logoView = new ImageView(logo);
            logoView.setFitWidth(200);
            logoView.setPreserveRatio(true);
            return logoView;
        } catch (Exception e) {
            System.out.println("Could not load logo image");
            return null;
        }
    }

    private boolean authenticate(String username, String password) {
        return "admin".equals(username) && "1234".equals(password);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Owner {
        private String name;
        private String id;
        protected String phoneNumber;
        private ObservableList<Animal> pets = FXCollections.observableArrayList();

        public Owner() {}
        
        public Owner(String name, String id, String phoneNumber) {
            this.name = name;
            this.id = id;
            this.phoneNumber = phoneNumber;
        }

        public String getName() {
            return name; 
        }
        public void setName(String name) { 
            this.name = name; 
        }
        public String getId() { 
            return id; 
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getPhoneNumber() {
            return phoneNumber; 
        }
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber; 
        }
        public ObservableList<Animal> getPets() { 
            return pets; 
        }

        public void addPet(Animal animal) {
            pets.add(animal);
        }

        public void removePet(Animal animal) {
            pets.remove(animal);
        }

        @Override
        public String toString() {
            return name;
        }
        
        public boolean equals(Owner other) {
            return this.name.equals(other.name) && this.phoneNumber.equals(other.phoneNumber);
        }
    } 
    
    
    public interface Serviceable {
        void provideService(String type);
    }

    public abstract class Animal implements Serviceable {
        protected String name;
        private int age;
        private String type;

        public Animal() {}
        public Animal(String name, int age, String type) {
            this.name = name;
            this.age = age;
            this.type = type;
        }

        public StringProperty nameProperty() { return new SimpleStringProperty(name); }
        public IntegerProperty ageProperty() { return new SimpleIntegerProperty(age); }
        public StringProperty typeProperty() { return new SimpleStringProperty(type); }
        public StringProperty detailsProperty() { return new SimpleStringProperty(toString()); }

        public String getName() { return name; }
        public int getAge() { return age; }
        public String getType() { return type; }
        public void setName(String name) { this.name = name; }
        public void setAge(int age) { this.age = age; }
        public void setType(String type) { this.type = type; }

        @Override
        public String toString() {
            return "Type: " + type + ", Name: " + name + ", Age: " + age;
        }

        public abstract String getPrice();
    }

    public class Dog extends Animal {
        private String breed;

        public Dog(String name, int age, String breed) {
            super(name, age, "Dog");
            this.breed = breed;
        }

        @Override
        public void provideService(String type) {
            System.out.println("Providing " + type + " service to dog (" + breed + ")");
        }

        @Override
        public String getPrice() {
            return "Price: 250-300 SAR";
        }

        @Override
        public String toString() {
            return super.toString() + ", Breed: " + breed + ", " + getPrice();
        }

        public String getBreed() { return breed; }
        public void setBreed(String breed) { this.breed = breed; }
    }

    
    public class Cat extends Animal {
        private boolean indoor;

        public Cat(String name, int age, boolean indoor) {
            super(name, age, "Cat");
            this.indoor = indoor;
        }

        @Override
        public void provideService(String type) {
            System.out.println("Providing " + type + " service to " + (indoor ? "indoor" : "") + " cat");
        }

        @Override
        public String getPrice() {
            return "Price: 150-200 SAR" + (indoor ? " (indoor discount)" : "");
        }

        @Override
        public String toString() {
            return super.toString() + ", Indoor: " + indoor + ", " + getPrice();
        }

        public boolean isIndoor() { return indoor; }
        public void setIndoor(boolean indoor) { this.indoor = indoor; }
    }

    public class Bird extends Animal {
        private boolean canFly;

        public Bird(String name, int age, boolean canFly) {
            super(name, age, "Bird");
            this.canFly = canFly;
        }

        @Override
        public void provideService(String type) {
            System.out.println("Providing " + type + " service to " + (canFly ? "flying" : "non-flying") + " bird");
        }

        @Override
        public String getPrice() {
            return "Price: 100-150 SAR" + (canFly ? "" : " (extra care needed)");
        }

        @Override
        public String toString() {
            return super.toString() + ", Can fly: " + canFly + ", " + getPrice();
        }

        public boolean isCanFly() { return canFly; }
        public void setCanFly(boolean canFly) { this.canFly = canFly; }
    }

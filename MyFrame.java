 
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MyFrame extends JFrame {
    private JTextField nameField, mobileField;
    private JRadioButton maleButton, femaleButton;
    private ButtonGroup genderGroup;
    private JSpinner dobSpinner;
    private JTextArea addressArea;
    private JCheckBox termsCheckBox;
    private Connection conn;
    private DefaultTableModel tableModel;
    private JTable table;

    public MyFrame() {
        initializeDatabase();
        setTitle("Registration Form");
        
        // Main panel with form
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Components
        JLabel nameLabel = new JLabel("Name");
        nameField = new JTextField(20);

        JLabel mobileLabel = new JLabel("Mobile");
        mobileField = new JTextField(20);

        JLabel genderLabel = new JLabel("Gender");
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        maleButton = new JRadioButton("Male");
        femaleButton = new JRadioButton("Female");
        genderGroup = new ButtonGroup();
        genderGroup.add(maleButton);
        genderGroup.add(femaleButton);
        genderPanel.add(maleButton);
        genderPanel.add(femaleButton);

        JLabel dobLabel = new JLabel("DOB");
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dobSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dobSpinner, "dd-MMM-yyyy");
        dobSpinner.setEditor(dateEditor);

        JLabel addressLabel = new JLabel("Address");
        addressArea = new JTextArea(4, 20);
        addressArea.setLineWrap(true);
        JScrollPane addressScrollPane = new JScrollPane(addressArea);

        termsCheckBox = new JCheckBox("Accept Terms And Conditions");

        // Add components to form panel
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(mobileLabel);
        formPanel.add(mobileField);
        formPanel.add(genderLabel);
        formPanel.add(genderPanel);
        formPanel.add(dobLabel);
        formPanel.add(dobSpinner);
        formPanel.add(addressLabel);
        formPanel.add(addressScrollPane);
        formPanel.add(new JLabel()); // Empty space
        formPanel.add(termsCheckBox);

        // Create table for displaying records
        String[] columnNames = {"Name", "Mobile", "Gender", "DOB", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> registerUser());
        buttonPanel.add(registerButton);

        // Main layout
        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.WEST);
        add(tableScrollPane, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);

        // Frame settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        loadExistingRecords();
    }

    private void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/sys",
                "root",
                "ichanyange496"
            );
            createTable();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
        }
    }

    private void createTable() {
        try (Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS registrations " +
                        "(name VARCHAR(100), " +
                        "mobile VARCHAR(15), " +
                        "gender VARCHAR(10), " +
                        "dob DATE, " +
                        "address TEXT)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void registerUser() {
        if (!validateForm()) {
            return;
        }

        try {
            String sql = "INSERT INTO registrations (name, mobile, gender, dob, address) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nameField.getText());
                pstmt.setString(2, mobileField.getText());
                pstmt.setString(3, maleButton.isSelected() ? "Male" : "Female");
                pstmt.setDate(4, new java.sql.Date(((Date) dobSpinner.getValue()).getTime()));
                pstmt.setString(5, addressArea.getText());
                pstmt.executeUpdate();
            }
            
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Registration successful!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty() ||
            mobileField.getText().trim().isEmpty() ||
            (!maleButton.isSelected() && !femaleButton.isSelected()) ||
            addressArea.getText().trim().isEmpty() ||
            !termsCheckBox.isSelected()) {
            
            JOptionPane.showMessageDialog(this, "Please fill all fields and accept terms");
            return false;
        }
        return true;
    }

    private void loadExistingRecords() {
        try {
            String sql = "SELECT * FROM registrations";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("name"),
                        rs.getString("mobile"),
                        rs.getString("gender"),
                        new SimpleDateFormat("dd-MMM-yyyy").format(rs.getDate("dob")),
                        rs.getString("address")
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        loadExistingRecords();
    }

    private void clearForm() {
        nameField.setText("");
        mobileField.setText("");
        genderGroup.clearSelection();
        dobSpinner.setValue(new Date());
        addressArea.setText("");
        termsCheckBox.setSelected(false);
    }

    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new MyFrame().setVisible(true));
    }
}
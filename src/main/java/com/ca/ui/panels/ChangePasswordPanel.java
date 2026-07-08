package com.ca.ui.panels;

import com.ca.core.MemoryRepository;
import com.ca.db.model.LoginUser;
import com.gt.common.ResourceManager;
import com.gt.common.constants.StrConstants;
import com.gt.common.utils.StringUtils;
import com.gt.common.utils.UIUtils;
import com.gt.uilib.components.AbstractFunctionPanel;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordPanel extends AbstractFunctionPanel {
    private JPanel innerPanel;
    private JTextField userName;
    private JPasswordField passWord;
    private JTextField txtNewUsrName;
    private JPasswordField txtNewPass1;
    private JPasswordField txtNewpass;

    public ChangePasswordPanel() {
        add(getLoginPanel());
        init();
    }

    public static void main(String[] args) throws Exception {
        if (SystemUtils.IS_OS_WINDOWS) {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        EventQueue.invokeLater(() -> {
            try {
                JFrame jf = new JFrame();
                ChangePasswordPanel panel = new ChangePasswordPanel();
                jf.setBounds(panel.getBounds());
                jf.getContentPane().add(panel);
                jf.setVisible(true);
                jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private JPanel getLoginPanel() {
        JPanel fullPanel = new JPanel();
        fullPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        innerPanel = new JPanel();
        innerPanel.setBounds(41, 96, 336, 144);
        fullPanel.add(innerPanel);
        innerPanel.setLayout(new FormLayout(new ColumnSpec[]{FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(18dlu;default)"),
                FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("max(21dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(61dlu;default):grow"),
                FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(65dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("max(65dlu;default)"),}, new RowSpec[]{FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(20dlu;default)"),
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(16dlu;default)"),
                FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(16dlu;default)"), FormFactory.RELATED_GAP_ROWSPEC,
                RowSpec.decode("max(17dlu;default)"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(17dlu;default)"),
                FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(18dlu;default)"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,}));

        JLabel lblImg = new JLabel("img");
        lblImg.setIcon(ResourceManager.getImageIcon("logo2.png"));
        innerPanel.add(lblImg, "2, 6, 3, 5");

        JLabel lblTitile = new JLabel(ResourceManager.getString(StrConstants.COMPANY_NAME));
        lblTitile.setFont(new Font("Tahoma", Font.BOLD, 16));
        innerPanel.add(lblTitile, "6, 6, 7, 1, left, default");

        JLabel lblDept = new JLabel(ResourceManager.getString(StrConstants.DEPARTMENT));
        lblDept.setFont(new Font("Tahoma", Font.BOLD, 13));
        innerPanel.add(lblDept, "6, 8, 7, 1, left, default");

        JLabel lblInventoryManagementSystem = new JLabel(ResourceManager.getString(StrConstants.APP_TITLE));
        lblInventoryManagementSystem.setFont(new Font("Tahoma", Font.BOLD, 13));
        innerPanel.add(lblInventoryManagementSystem, "6, 10, 7, 1, left, default");

        JLabel lblNewLabel = new JLabel("Usuario :");
        innerPanel.add(lblNewLabel, "4, 14, left, default");

        userName = new JTextField();
        innerPanel.add(userName, "8, 14, 3, 1, fill, fill");


        JLabel lblPassword = new JLabel("Contraseña :");
        innerPanel.add(lblPassword, "4, 16, left, default");

        passWord = new JPasswordField();


        innerPanel.add(passWord, "8, 16, 3, 1, fill, fill");

        JLabel lblNewUsername = new JLabel("Nuevo usuario:");
        innerPanel.add(lblNewUsername, "4, 18");

        txtNewUsrName = new JTextField();
        innerPanel.add(txtNewUsrName, "8, 18, 3, 1, fill, fill");
        txtNewUsrName.setColumns(10);

        JLabel lblNewPassword = new JLabel("Nueva contraseña");
        innerPanel.add(lblNewPassword, "4, 20");

        txtNewPass1 = new JPasswordField();
        innerPanel.add(txtNewPass1, "8, 20, 3, 1, fill, fill");

        txtNewpass = new JPasswordField();
        innerPanel.add(txtNewpass, "8, 22, 3, 1, fill, fill");

        JButton loginButton = new JButton("Cambiar");
        innerPanel.add(loginButton, "8, 24, fill, default");
        JButton restPassword = new JButton("Limpiar");
        restPassword.addActionListener(e -> clearAll());
        loginButton.addActionListener(e -> change());
        return fullPanel;
    }

    private void clearAll() {
        UIUtils.clearAllFields(innerPanel);

    }

    private void change() {

        if (StringUtils.isEmpty(txtNewpass.getText()) || StringUtils.isEmpty(userName.getText()) || StringUtils.isEmpty(passWord.getText())
                || StringUtils.isEmpty(txtNewPass1.getText())) {
            JOptionPane.showMessageDialog(null, "Ingrese correctamente el usuario y la contraseña");
            return;
        }
        if (!txtNewpass.getText().trim().equals(txtNewPass1.getText().trim())) {
            JOptionPane.showMessageDialog(null, "La nueva contraseña y la confirmación no coinciden");
            return;
        }

        try {
            LoginUser user = MemoryRepository.getInstance().getLoginUser(userName.getText().trim(), passWord.getText().trim());
            if (user == null) {
                JOptionPane.showMessageDialog(null, "Error de usuario o contraseña original");
                passWord.setText("");
                userName.requestFocus();
                return;
            }

            // finally change password/username
            MemoryRepository.getInstance().changeLogin(txtNewUsrName.getText().trim(), txtNewpass.getText().trim());

            JOptionPane.showMessageDialog(null, "Usuario/Contraseña cambiados exitosamente\nNuevo usuario: " + txtNewUsrName.getText().trim());
            clearAll();
        } catch (Exception e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error de conexión");
        }

    }

    @Override
    public final String getFunctionName() {
        return "Cambiar Contraseña";
    }

    @Override
    public void handleSaveAction() {

    }

    @Override
    public final void init() {
        super.init();
        userName.requestFocus();
        isReadyToClose = true;
    }

    @Override
    public void enableDisableComponents() {
        // TODO Auto-generated method stub

    }

}
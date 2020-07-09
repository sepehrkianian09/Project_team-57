package client.view;

import com.jfoenix.controls.JFXProgressBar;
import server.controller.Control;
import server.controller.account.AccountControl;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class AccountProcessor extends Processor{
    protected AccountControl accountControl = AccountControl.getController();
    public ImageView startMediaButton;
    public ImageView nextMediaButton;
    public ImageView previousMediaButton;
    public Label mediaNameLabel;
    public Label mediaArtistLabel;
    public JFXProgressBar mediaProgressBar;

    public void logOutButton() {
        Optional<ButtonType> buttonType =
                new Alert(Alert.AlertType.CONFIRMATION, "Are You Sure About Logging Out?", ButtonType.YES, ButtonType.NO).showAndWait();
        if(buttonType.get() == ButtonType.YES) {
            subStages.forEach(stage -> {
                stage.close();
            });
            Control.logOut();
            backToMainMenu();
        }

    }

    public void backToMainMenu() {
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

    public void showProfileMenu() {
        try {
            if(Control.getType().equals("Admin")) {
                if (!canOpenSubStage(Control.getUsername() + " Profile", parentProcessor))
                    return;
            }
            else
                if(!canOpenSubStage(Control.getUsername() + " Profile", this))
                return;
            AccountControl accountControl = AccountControl.getController();
            ProfileProcessor.setAccount(accountControl.getAccount());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProfileMenu.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.getIcons().add(new Image(getClass().getResourceAsStream("Profile Icon.png")));
            newStage.setResizable(false);
            newStage.setTitle(Control.getUsername() + " Profile");
            if(Control.getType().equals("Admin"))
                parentProcessor.addSubStage(newStage);
            else
                addSubStage(newStage);
            newStage.show();
        } catch (IOException e) {
            //:)
        }
    }

    public void modifyPlayingMusic() {
        AccountControl.getController().modifyPlayingMusic().setOnEndOfMedia(() -> this.changeMusic(null));
        initMusicPlayer();
    }


    public void changeMusic(MouseEvent mouseEvent) {
        int k;
        if(mouseEvent != null) {
           k = (((ImageView)mouseEvent.getSource()).getId().equalsIgnoreCase("nextMediaButton") ? 1 : -1);
        } else {
            k = 1;
        }
        MediaPlayer mediaPlayer = accountControl.changeMusic(k);
        if(mediaPlayer != null) {
            mediaPlayer.setOnEndOfMedia(() -> this.changeMusic(null));
        }
        initMusicPlayer();
    }

    protected void initMusicPlayer() {
        setStartMediaButton();
        mediaArtistLabel.setText(accountControl.getPlayingMusic().getArtist());
        mediaNameLabel.setText(accountControl.getPlayingMusic().getName());
        if(accountControl.isMusicPlaying())
            mediaProgressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        else
            mediaProgressBar.setProgress(0);
    }

    private void setStartMediaButton() {
        String url = Control.getType() + " " + (accountControl.isMusicPlaying() ? "Pause" : "Start") + ".png";
        startMediaButton.setImage(new Image(IMAGE_FOLDER_URL + "Icons/MediaIcons/" + url));
    }

}
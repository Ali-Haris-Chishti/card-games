package com.ahccode.cards.card;

import com.ahccode.cards.network.message.CardMessage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

@Slf4j
public class Card extends ImageView implements Serializable {

    private static final long serialVersionUID = 1L;

    public CardFamily getCardFamily() {
        return cardFamily;
    }

    public CardNumber getCardNumber() {
        return cardNumber;
    }

    private CardFamily cardFamily;

    private CardNumber cardNumber;

    private final String BASE_PATH = "file:///" + System.getProperty("user.dir") + "\\cards";

    public Card(CardFamily cardFamily, CardNumber cardNumber, boolean show, boolean initImage) {

        this.cardFamily = cardFamily;
        this.cardNumber = cardNumber;
        log.info("Constructing Card: [{}, {}, {}]", cardFamily, cardNumber, BASE_PATH);

        if (initImage) {
            String path = null;
            if (show)
                path = BASE_PATH + "\\" + cardNumber + " of " + cardFamily + ".png";
            else
                path = BASE_PATH + "\\" + "BACK.png";
            setImage(new Image(path));
        }

//        String imageName = cardNumber + " of " + cardFamily + ".png";
//
//        try {
//            // Try URL encoding the filename
//            String encodedName = java.net.URLEncoder.encode(imageName, "UTF-8");
//            log.info("Encoded name: {}", encodedName);
//
//            if (show) {
//                log.info(getClass().getResourceAsStream(imageName).toString());
//                // Try both encoded and non-encoded
//                var stream = getClass().getResourceAsStream(imageName);
//                if (stream == null) {
//                    stream = getClass().getResourceAsStream(encodedName);
//                }
//                if (stream == null) {
//                    throw new RuntimeException("Could not find image: " + imageName);
//                }
//                setImage(new Image(stream));
//            } else {
//                setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("BACK.png"))));
//            }
//        } catch (Exception e) {
//            log.error("Error loading card image", e);
//            throw new RuntimeException(e);
//        }
    }

    public Card() {
        setImage(new Image(BASE_PATH + "\\" + "BACK.png"));
    }

    public void showCard(boolean show) {
        String path = null;
        if (show)
            path = BASE_PATH + "\\" + cardNumber + " of " + cardFamily + ".png";
        else
            path = BASE_PATH + "\\" + "BACK.png";
        setImage(new Image(
                path
        ));

    }


    public boolean equals(CardFamily family, CardNumber number) {
        return this.cardFamily == family && this.cardNumber == number;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != this.getClass())
            return false;
        Card c = (Card) o;
        return cardFamily.equals(c.cardFamily) && Objects.equals(cardNumber, c.cardNumber);
    }


    @Override
    public String toString() {
        return String.format("%s of %s", cardNumber, cardFamily);
    }

    public CardMessage toCardMessage() {
        return new CardMessage(cardFamily, cardNumber);
    }
}

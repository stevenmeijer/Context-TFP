package nl.tudelft.contextproject.gui;

import com.sun.jna.Memory;

import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import nl.tudelft.contextproject.ContextTFP;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

/**
 * Handler for streaming media into the GUI through VLC.
 * 
 * <p>Most credits to Mark Lee for creating this solution.
 * 
 * @see <a href="https://github.com/caprica/vlcj-javafx/tree/master/src/test/java/uk/co/caprica/vlcj/javafx/test">
 * Github Repo</a>
 * @since 0.6
 */
public class LiveStreamHandler {

    private PixelWriter pixelWriter;
    private WritablePixelFormat<ByteBuffer> pixelFormat;
    private DirectMediaPlayerComponent mediaPlayer;
    private String streamLink;
    private ImageView imageView;
    private FloatProperty videoSourceRatioProperty;

    /**
     * Creates a LiveStreamHandler object.
     */
    public LiveStreamHandler() {
        videoSourceRatioProperty = new SimpleFloatProperty(0.5625f);
    }

    /**
     * Starts playing the media.
     */
    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.getMediaPlayer().playMedia(streamLink);
        }
    }

    /**
     * Stops playing the media and release associated resources.
     */
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.getMediaPlayer().stop();
            mediaPlayer.getMediaPlayer().release();
            mediaPlayer = null;
        }
    }

    /**
     * Returns an ImageView object that can be put into the JavaFX framework.
     * 
     * @param streamLink link to media stream.
     * @param width Width of the ImageView.
     * @param height Height of the ImageView.
     * @return a ImageView object displaying the stream.
     */
    public ImageView createImageView(String streamLink, double width, double height) {
        if (!ContextTFP.hasVLC()) {
            return createErrorImageView();
        }

        WritableImage writableImage = new WritableImage((int) width, (int) height);
        imageView = new ImageView(writableImage);
        this.streamLink = streamLink;
        this.pixelWriter = writableImage.getPixelWriter();
        this.pixelFormat = PixelFormat.getByteBgraPreInstance();

        this.mediaPlayer = new DirectMediaPlayerComponent((sourceWidth, sourceHeight) -> {
            Platform.runLater( () -> {
                videoSourceRatioProperty.set((float) sourceHeight / (float) sourceWidth);
            });
            return new RV32BufferFormat((int) width, (int) height);
        }) {
            @Override
            public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
                Memory nativeBuffer = mediaPlayer.lock()[0];
                try {
                    ByteBuffer byteBuffer = nativeBuffer.getByteBuffer(0, nativeBuffer.size());
                    pixelWriter.setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), 
                            pixelFormat, byteBuffer, bufferFormat.getPitches()[0]);
                } finally {
                    mediaPlayer.unlock();
                }
            }
        };

        return imageView;
    }

    /**
     * Returns true if there is an active MediaPlayer.
     * @return True if there is an active MediaPlayer, otherwise false.
     */
    public boolean isPlaying() {
        return this.mediaPlayer != null;
    }

    /**
     * Returns an ImageView displaying an error symbol.
     * @return The created ImageView.
     */
    public ImageView createErrorImageView() {
        return new ImageView("error-x.png");
    }

    /**
     * Returns the ratio of the media currently playing.
     * @return Ratio of the media currently playing.
     */
    public FloatProperty getRatio() {
        return this.videoSourceRatioProperty;
    }

    /**
     * Returns the URL of the stream that is currently played.
     * @return The URL of the stream that is currently played.
     */
    public String getStreamLink() {
        return this.streamLink;
    }
    
    /**
     * Takes a snapshot of the image present on a camera and stores the image
     * at a chosen location.
     *
     * @param imageLocation The location to which the captured image is stored.
     */
    public void snapShot(String imageLocation) {
        if (imageView != null) {
            WritableImage image = imageView.snapshot(new SnapshotParameters(), null);
            File output = new File(imageLocation);

            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", output);
            } catch ( IOException e) {
                e.printStackTrace();
            }
        }
    }
}

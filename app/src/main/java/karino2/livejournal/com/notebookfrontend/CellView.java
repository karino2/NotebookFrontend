package karino2.livejournal.com.notebookfrontend;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import karino2.livejournal.com.notebookfrontend.Cell.CellType;

/**
 * Created by _ on 2017/05/29.
 */

public class CellView extends LinearLayout {

    Cell cell;
    CellType cellType = CellType.UNINITIALIZE;
    TextView sourceView;
    TextView outputConsoleView;
    TextView execCountView;
    ImageView outputImageView;
    FrameLayout outputFrame;

    public Cell getBoundCell() { return cell; }

    public CellView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    void ensureInitialize() {
        if(cellType == CellType.UNINITIALIZE) {
            execCountView = (TextView)findViewById(R.id.execCountView);
            sourceView = (TextView)findViewById(R.id.sourceView);
            outputConsoleView = (TextView)findViewById(R.id.outputConsoleView);
            outputImageView = (ImageView)findViewById(R.id.outputImageView);
            outputFrame = (FrameLayout)findViewById(R.id.outputFrame);

            cellType = CellType.CODE;
        }
    }

    void setupMarkdownCell() {
        ensureInitialize();
        execCountView.setVisibility(GONE);
        outputFrame.setVisibility(GONE);

        sourceView.setBackgroundColor(MARKDOWN_BG_COLOR);
        sourceView.setText(cell.getSource());
    }

    final int CODE_BG_COLOR = 0xffebebeb;
    final int MARKDOWN_BG_COLOR = 0xffffffff;

    void setupCodeCell() {
        ensureInitialize();
        outputFrame.setVisibility(VISIBLE);
        execCountView.setVisibility(VISIBLE);
        sourceView.setBackgroundColor(CODE_BG_COLOR);

        sourceView.setText(cell.getSource());

        if(cell.executionCount != null) {
            if(cell.executionCount == Cell.EXEC_COUNT_RUNNING) {
                execCountView.setText("[*]");
            } else {
                execCountView.setText("[" + cell.executionCount + "]");
            }
        }

        Cell.Output output = cell.getOutput();
        if(output == null) {
            setTextOutput("");
            return;
        }
        if(output.isImage()) {
            String base64image = output.getImageAsBase64();
            setImageOutput(base64image);
            return;
        }
        setTextOutput(output.getText());


    }

    private void setTextOutput(String outtext) {
        outputConsoleView.setText(outtext);

        outputImageView.setVisibility(GONE);
        outputConsoleView.setVisibility(VISIBLE);
    }

    void setImageOutput(String base64image) {
        if(base64image == null)
        {
            Log.d("NotebookFrontend", "base64 image is null, which case?");
            return;
        }
        byte[] decodedBytes = Base64.decode(base64image, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        outputImageView.setImageBitmap(bmp);

        outputImageView.setVisibility(VISIBLE);
        outputConsoleView.setVisibility(GONE);
    }

    public void bindCell(Cell cell) {
        this.cell = cell;

        CellType ctype = cell.getCellType();
        switch(ctype) {
            case CODE:
                setupCodeCell();
                break;
            case MARKDOWN:
                setupMarkdownCell();
                break;
            default:
                break;
        }
    }
}

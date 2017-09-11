package fi.viinikoodi.result;

import com.google.zxing.Result;
import fi.viinikoodi.client.android.R;

import fi.viinikoodi.result.ResultHandler;
import com.google.zxing.client.result.ParsedResult;

import android.app.Activity;


/**
 * Handles generic products which are not books.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class WineResultHandler extends ResultHandler {
  private static final int[] buttons = {
//      R.string.button_product_search,
//      R.string.button_web_search,
//      R.string.button_custom_product_search
  };

  public WineResultHandler(Activity activity, ParsedResult result, Result rawResult) {
    super(activity, result, rawResult);
  }
  
  @Override
  public int getButtonCount() {
    return hasCustomProductSearch() ? buttons.length : buttons.length - 1;
  }

  @Override
  public int getButtonText(int index) {
    return buttons[index];
  }

  @Override
  public void handleButtonPress(final int index) {
    /*
	showNotOurResults(index, new AlertDialog.OnClickListener() {
      public void onClick(DialogInterface dialogInterface, int i) {
        ProductParsedResult productResult = (ProductParsedResult) getResult();
        switch (index) {
          case 0:
            openProductSearch(productResult.getNormalizedProductID());
            break;
          case 1:
            webSearch(productResult.getNormalizedProductID());
            break;
          case 2:
            openURL(fillInCustomSearchURL(productResult.getNormalizedProductID()));
            break;
        }
      }
    });
    */
  }

  @Override
  public int getDisplayTitle() {
    return R.string.result_product;
  }
}

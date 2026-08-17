package io.github.sds100.keymapper;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBinderMapperImpl extends DataBinderMapper {
  private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(0);

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View view, int layoutId) {
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = view.getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View[] views, int layoutId) {
    if(views == null || views.length == 0) {
      return null;
    }
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = views[0].getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
      }
    }
    return null;
  }

  @Override
  public int getLayoutId(String tag) {
    if (tag == null) {
      return 0;
    }
    Integer tmpVal = InnerLayoutIdLookup.sKeys.get(tag);
    return tmpVal == null ? 0 : tmpVal;
  }

  @Override
  public String convertBrIdToString(int localId) {
    String tmpVal = InnerBrLookup.sKeys.get(localId);
    return tmpVal;
  }

  @Override
  public List<DataBinderMapper> collectDependencies() {
    ArrayList<DataBinderMapper> result = new ArrayList<DataBinderMapper>(2);
    result.add(new androidx.databinding.library.baseAdapters.DataBinderMapperImpl());
    result.add(new io.github.sds100.keymapper.base.DataBinderMapperImpl());
    return result;
  }

  private static class InnerBrLookup {
    static final SparseArray<String> sKeys = new SparseArray<String>(24);

    static {
      sKeys.put(0, "_all");
      sKeys.put(1, "allowEmpty");
      sKeys.put(2, "caption");
      sKeys.put(3, "customerId");
      sKeys.put(4, "emptyListPlaceholder");
      sKeys.put(5, "hint");
      sKeys.put(6, "icon");
      sKeys.put(7, "model");
      sKeys.put(8, "nameTextWatcher");
      sKeys.put(9, "onCheckedChange");
      sKeys.put(10, "onChooseKeyCodeClick");
      sKeys.put(11, "onClickListener");
      sKeys.put(12, "onFixClick");
      sKeys.put(13, "onRemoveClick");
      sKeys.put(14, "onSelectScreenshotClick");
      sKeys.put(15, "onShowExampleClick");
      sKeys.put(16, "pinchTypeSpinnerAdapter");
      sKeys.put(17, "subtitle");
      sKeys.put(18, "text");
      sKeys.put(19, "title");
      sKeys.put(20, "url");
      sKeys.put(21, "valueTextWatcher");
      sKeys.put(22, "version");
      sKeys.put(23, "viewModel");
    }
  }

  private static class InnerLayoutIdLookup {
    static final HashMap<String, Integer> sKeys = new HashMap<String, Integer>(0);
  }
}

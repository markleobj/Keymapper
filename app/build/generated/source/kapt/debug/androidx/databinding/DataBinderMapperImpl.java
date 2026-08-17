package androidx.databinding;

public class DataBinderMapperImpl extends MergedDataBinderMapper {
  DataBinderMapperImpl() {
    addMapper(new io.github.sds100.keymapper.DataBinderMapperImpl());
  }
}

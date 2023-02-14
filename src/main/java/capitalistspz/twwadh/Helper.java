package capitalistspz.twwadh;

import java.util.List;

public class Helper {

    public static<T> void Reverse(List<T> list){
        final int resultCount = list.size();
        for (int i = 0; i < resultCount / 2; ++i){
            var temp = list.get(resultCount - 1 - i);
            list.set(resultCount - 1 - i, list.get(i));
            list.set(i, temp);
        }
    }
}

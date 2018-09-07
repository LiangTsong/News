package database.NewsDbSchema;

public class NewsDbSchema {
    public static final class Newstable{
        public static final String NAME = "news";

        public static final class Cols{
            public static final String TITLE = "title";
            public static final String DESCRIPTION = "description";
            public static final String PUBDATE = "pubdate";
            public static final String LINK = "link";
            public static final String TYPE = "type";
            public static final String READ = "read";
            public static final String SAVED = "saved";
            public static final String HTML = "html";

        }
    }
}

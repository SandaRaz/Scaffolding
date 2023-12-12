package generator;
 public class TypeAndName {
    String columnName;
    String columnType;

     public String getColumnName() {
         return columnName;
     }

     public void setColumnName(String columnName) {
         this.columnName = columnName;
     }

     public String getColumnType() {
         return columnType;
     }

     public void setColumnType(String columnType) {
         this.columnType = columnType;
     }

     public TypeAndName(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }
}

package generator;
 public class TypeAndName {
    String columnName;
    String columnType;
    boolean isPrimaryKey;
    boolean isForeignKey;
    String referencedTable;
    String databaseType;

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

     public boolean isPrimaryKey() {
         return isPrimaryKey;
     }

     public void setPrimaryKey(boolean primaryKey) {
         isPrimaryKey = primaryKey;
     }

     public boolean isForeignKey() {
         return isForeignKey;
     }

     public void setForeignKey(boolean foreignKey) {
         isForeignKey = foreignKey;
     }

     public String getReferencedTable() {
         return referencedTable;
     }

     public void setReferencedTable(String referencedTable) {
         this.referencedTable = referencedTable;
     }

     public String getDatabaseType() {
         return databaseType;
     }

     public void setDatabaseType(String databaseType) {
         this.databaseType = databaseType;
     }

     public TypeAndName() {
     }

     public TypeAndName(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }
     public TypeAndName(String columnName, String columnType,String databaseType) {
         this.columnName = columnName;
         this.columnType = columnType;
         this.databaseType = databaseType;
     }

     public TypeAndName(String columnName, String columnType, boolean isPrimaryKey, boolean isForeignKey, String referencedTable, String databaseType) {
         this.columnName = columnName;
         this.columnType = columnType;
         this.isPrimaryKey = isPrimaryKey;
         this.isForeignKey = isForeignKey;
         this.referencedTable = referencedTable;
         this.databaseType = databaseType;
     }
 }

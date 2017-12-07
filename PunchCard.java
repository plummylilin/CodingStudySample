import java.io.Serializable;

public class PunchCard implements Serializable{
	//serialVersionUID值
    private static final long serialVersionUID=1L;
    //数据定义
    private String PunchCardId;
	private String Category;
    private String SubCategory;
    private String Hours;
    private String Note;
    private Boolean IsUseful;
    
    /**
     * 默认无参数的构造方法
     */
    public PunchCard(){
         
    }
    //构造函数
    public PunchCard(String PunchCardId){
        this.PunchCardId=PunchCardId;
    }
     
    //GET SET
    public String getPunchCardId() {
		return PunchCardId;
	}
	public void setPunchCardId(String punchCardId) {
		PunchCardId = punchCardId;
	}
	public String getCategory() {
		return Category;
	}
	public void setCategory(String category) {
		Category = category;
	}
	public String getSubCategory() {
		return SubCategory;
	}
	public void setSubCategory(String subCategory) {
		SubCategory = subCategory;
	}
	public String getHours() {
		return Hours;
	}
	public void setHours(String hours) {
		Hours = hours;
	}
	public String getNote() {
		return Note;
	}
	public void setNote(String note) {
		Note = note;
	}
	public Boolean getIsUseful() {
		return IsUseful;
	}
	public void setIsUseful(Boolean isUseful) {
		IsUseful = isUseful;
	}

}

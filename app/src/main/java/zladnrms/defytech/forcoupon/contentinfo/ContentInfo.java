package zladnrms.defytech.forcoupon.contentinfo;

public class ContentInfo { // 게임방 정보 클래스

    private int contentId;
    private String contentName;
    private String contentText;
    private int contentSize;
    private String contentColor;

    public ContentInfo(int _id, String _name, String _text, int _size, String _color) {
        this.contentId = _id;
        this.contentName = _name;
        this.contentText = _text;
        this.contentSize = _size;
        this.contentColor = _color;
    }

    public int getId() {
        return contentId;
    }

    public String getText() {
        return contentText;
    }

    public String getName() {
        return contentName;
    }

    public int getSize() {
        return contentSize;
    }

    public String getColor() {
        return contentColor;
    }
}
package zladnrms.defytech.forcoupon.contentinfo;

public class BgrdInfo { // 게임방 정보 클래스

    private int bgrdId;
    private String bgrdName;

    public BgrdInfo(int _id, String _name) {
        this.bgrdId = _id;
        this.bgrdName = _name;
    }

    public int getId() {
        return bgrdId;
    }

    public String getName() {
        return bgrdName;
    }
}
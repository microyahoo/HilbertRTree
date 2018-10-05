package com.njupt.cluster;

public class General {
	
	private String name; // 姓名
	private int render; // 星级
	private int tongshai; // 统帅
	private int wuli; // 武力
	private int zhili; // 智力
	private int polic; // 政治
	private int qiangbin; // 枪兵
	private int jibin; // 戟兵
	private int nubin; // 弩兵
	private int qibin; // 骑兵
	private int binqi; // 兵器
	private int tongwu; // 统武
	private int tongzhi; // 统智
	private int tongwuzhi; // 统武智
	private int tongwuzhizheng; // 统武智政
	private int salary; // 50级工资

	public General(int render, String name, int tongshai, int wuli, int zhili,
			int polic, int qiangbin, int jibin, int nubin, int qibin,
			int binqi, int tongwu, int tongzhi, int tongwuzhi,
			int tongwuzhizheng, int salary) {
		super();
		this.name = name;
		this.render = render;
		this.tongshai = tongshai;
		this.wuli = wuli;
		this.zhili = zhili;
		this.polic = polic;
		this.qiangbin = qiangbin;
		this.jibin = jibin;
		this.nubin = nubin;
		this.qibin = qibin;
		this.binqi = binqi;
		this.tongwu = tongwu;
		this.tongzhi = tongzhi;
		this.tongwuzhi = tongwuzhi;
		this.tongwuzhizheng = tongwuzhizheng;
		this.salary = salary;
	}

	public General(int render, int tongshai, int wuli, int zhili, int polic,
			int qiangbin, int jibin, int nubin, int qibin, int binqi,
			int tongwu, int tongzhi, int tongwuzhi, int tongwuzhizheng,
			int salary) {
		super();
		this.name = "聚类中心";
		this.render = render; 
		this.tongshai = tongshai; 
		this.wuli = wuli;
		this.zhili = zhili;
		this.polic = polic;
		this.qiangbin = qiangbin;
		this.jibin = jibin;
		this.nubin = nubin;
		this.qibin = qibin;
		this.binqi = binqi;
		this.tongwu = tongwu;
		this.tongzhi = tongzhi;
		this.tongwuzhi = tongwuzhi;
		this.tongwuzhizheng = tongwuzhizheng;
		this.salary = salary;
	}

	public General() {
	}

	@Override
	public String toString() {
		return "武将 [name=" + name + ", render=" + Tool.dxingji(render)
				+ ", tongshai=" + tongshai + ", wuli=" + wuli + ", zhili="
				+ zhili + ", polic=" + polic + ", qiangbin="
				+ Tool.dchange(qiangbin) + ", jibin=" + Tool.dchange(jibin)
				+ ", nubin=" + Tool.dchange(nubin) + ", qibin="
				+ Tool.dchange(qibin) + ", binqi=" + Tool.dchange(binqi)
				+ ", tongwu=" + tongwu + ", tongzhi=" + tongzhi
				+ ", tongwuzhi=" + tongwuzhi + ", tongwuzhizheng="
				+ tongwuzhizheng + ", salary=" + salary + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRender() {
		return render;
	}

	public void setRender(int render) {
		this.render = render;
	}

	public int getTongshai() {
		return tongshai;
	}

	public void setTongshai(int tongshai) {
		this.tongshai = tongshai;
	}

	public int getWuli() {
		return wuli;
	}

	public void setWuli(int wuli) {
		this.wuli = wuli;
	}

	public int getZhili() {
		return zhili;
	}

	public void setZhili(int zhili) {
		this.zhili = zhili;
	}

	public int getPolic() {
		return polic;
	}

	public void setPolic(int polic) {
		this.polic = polic;
	}

	public int getQiangbin() {
		return qiangbin;
	}

	public void setQiangbin(int qiangbin) {
		this.qiangbin = qiangbin;
	}

	public int getJibin() {
		return jibin;
	}

	public void setJibin(int jibin) {
		this.jibin = jibin;
	}

	public int getNubin() {
		return nubin;
	}

	public void setNubin(int nubin) {
		this.nubin = nubin;
	}

	public int getQibin() {
		return qibin;
	}

	public void setQibin(int qibin) {
		this.qibin = qibin;
	}

	public int getBinqi() {
		return binqi;
	}

	public void setBinqi(int binqi) {
		this.binqi = binqi;
	}

	public int getTongwu() {
		return tongwu;
	}

	public void setTongwu(int tongwu) {
		this.tongwu = tongwu;
	}

	public int getTongzhi() {
		return tongzhi;
	}

	public void setTongzhi(int tongzhi) {
		this.tongzhi = tongzhi;
	}

	public int getTongwuzhi() {
		return tongwuzhi;
	}

	public void setTongwuzhi(int tongwuzhi) {
		this.tongwuzhi = tongwuzhi;
	}

	public int getTongwuzhizheng() {
		return tongwuzhizheng;
	}

	public void setTongwuzhizheng(int tongwuzhizheng) {
		this.tongwuzhizheng = tongwuzhizheng;
	}

	public int getSalary() {
		return salary;
	}

	public void setSalary(int salary) {
		this.salary = salary;
	}

}

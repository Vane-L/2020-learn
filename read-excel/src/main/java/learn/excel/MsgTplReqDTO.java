package learn.excel;


/**
 * @Author: wenhongliang
 */
public class MsgTplReqDTO {
    private Integer tplId;
    private String tplName;
    private Integer tplChannel;
    private String tplDesc;

    private EmailTpl emailTpl;
    private SmsTpl smsTpl;
    private PnTpl pnTpl;
    private ArTpl arTpl;

    static class SmsTpl {
        private Integer type;
        private String content;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    static class EmailTpl {
        private Integer type;
        private String subject;
        private String content;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    static class PnTpl {
        private Integer type;
        private String title;
        private String content;
        private String redirectUrl;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }
    }

    static class ArTpl {
        private Integer type;
        private String title;
        private String content;
        private String redirectUrl;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }

        public void setRedirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }
    }

    public Integer getTplId() {
        return tplId;
    }

    public void setTplId(Integer tplId) {
        this.tplId = tplId;
    }

    public String getTplName() {
        return tplName;
    }

    public void setTplName(String tplName) {
        this.tplName = tplName;
    }

    public Integer getTplChannel() {
        return tplChannel;
    }

    public void setTplChannel(Integer tplChannel) {
        this.tplChannel = tplChannel;
    }

    public String getTplDesc() {
        return tplDesc;
    }

    public void setTplDesc(String tplDesc) {
        this.tplDesc = tplDesc;
    }

    public EmailTpl getEmailTpl() {
        return emailTpl;
    }

    public void setEmailTpl(EmailTpl emailTpl) {
        this.emailTpl = emailTpl;
    }

    public SmsTpl getSmsTpl() {
        return smsTpl;
    }

    public void setSmsTpl(SmsTpl smsTpl) {
        this.smsTpl = smsTpl;
    }

    public PnTpl getPnTpl() {
        return pnTpl;
    }

    public void setPnTpl(PnTpl pnTpl) {
        this.pnTpl = pnTpl;
    }

    public ArTpl getArTpl() {
        return arTpl;
    }

    public void setArTpl(ArTpl arTpl) {
        this.arTpl = arTpl;
    }
}

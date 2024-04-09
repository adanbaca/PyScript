public class CommandReturn {

    public ReturnType returnType;
    public String msg;

    public CommandReturn(ReturnType returnType, String msg) {
        this.returnType = returnType;
        this.msg = msg;
    }

}

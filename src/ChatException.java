//定义在map中未找到对应用户的异常
class NotFoundinMapException extends Exception{
    public NotFoundinMapException(){
        super();
    }
}
//定义在map中已经有重复的uid的异常
class RenameInMapException extends Exception{
    public isErrorMessage isErrorMes;
    public RenameInMapException(){
        super();
    }

    public RenameInMapException(isErrorMessage isErrorMes) {
        this.isErrorMes = isErrorMes;
    }
}
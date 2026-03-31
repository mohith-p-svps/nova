class ReturnException extends RuntimeException {
    Value value;

    ReturnException(Value value) {
        this.value = value;
    }
}
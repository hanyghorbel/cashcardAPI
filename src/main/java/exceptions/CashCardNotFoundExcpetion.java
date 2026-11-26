package exceptions;

public class CashCardNotFoundExcpetion extends RuntimeException{
    public CashCardNotFoundExcpetion(Long id){
        super("CashCard not found with id: " + id);
    }
}

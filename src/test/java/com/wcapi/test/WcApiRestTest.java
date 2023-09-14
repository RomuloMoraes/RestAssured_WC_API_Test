package com.wcapi.test;

import com.wcapi.core.BaseTest;
import com.wcapi.core.MovementPT;
import com.wcapi.core.MovementENG;
import org.hamcrest.Matchers;
import org.junit.*;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class WcApiRestTest extends BaseTest {

    private String TOKEN;

    private static String ACCOUNT_NAME = "User " + System.nanoTime();

    private static Integer ACCOUNT_ID;

    /*this method will run before each test in this class
    this method will extract the token from the response
     */
    @Before
    public void loginExtractToken() {
        //creating a map
        Map<String, String> login = new HashMap<>();
        login.put("email", "limamoraes_7@hotmail.com");
        login.put("senha", "123456");

        //extract the token
        TOKEN = given()
                .body(login)// the login is passed in the body
                .when()
                .post("/signin")
                .then()
                .statusCode(200)
                .extract().path("token");

        //System.out.println(token);
    }

    public MovementPT createMovementValida() {
        MovementPT mov = new MovementPT();
        mov.setConta_id(1562804);
        //mov.setUsuario_id();
        mov.setDescricao("Financial movement");
        mov.setEnvolvido("Involved in transaction");
        mov.setTipo("REC");
        mov.setData_transacao("01/01/2020");
        mov.setData_pagamento("15/01/2020");
        mov.setValor(100f);
        mov.setStatus(true);
        return mov;
    }

    public Integer createUserAccount() {

        loginExtractToken();

        Integer ID = given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\"nome\" : \"" + ACCOUNT_NAME + "\"}")
                .when()
                .post("/contas")
                .then()
                .statusCode(201)
                .extract().path("id")
        ;
        return ID;
    }

    @Test
    public void whenRequestAccessWithoutToken_thenAccessNotGranted() {

        given()
                .when()
                .get("/contas")
                .then()
                .statusCode(401)
        ;
    }

    @Test
    public void whenRequestIncludingAccount_thenAccountIsIncluded() {

        given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\"nome\" : \"" + ACCOUNT_NAME + "\"}")
                .when()
                .post("/contas")
                .then()
                .statusCode(201)
        ;


    }

    @Test
    public void whenRequestToUpdateAccount_thenAccountIsUpdated() {

        ACCOUNT_ID = createUserAccount();

        given()
                .log().all()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\"nome\" : \"" + ACCOUNT_NAME + " Updated\"}")
                .pathParam("id", ACCOUNT_ID)
                .when()
                .put("contas/{id}")
                .then()
                .log().all()
                .statusCode(200)
                .body("nome", is(ACCOUNT_NAME + " Updated"))
                .body("id", is(ACCOUNT_ID))
        ;
    }

    @Test
    public void whenInsertAccountWithSameName_thenNotAuthorized() {
        given()
                .header("Authorization", "JWT " + TOKEN)
                .body("{\"nome\" : \"Beltrano Barros\"}")
                .when()
                .post("/contas")
                .then()
                .log().all()
                .statusCode(400)
                .body("error", is("Já existe uma conta com esse nome!"))
        ;


    }

    /*@Test
    public void whenInsertAMovementENG_thenOperationOk() {
        MovementENG mov = new MovementENG();
        mov.setAccountId(1562804);
        //mov.setUserId();
        mov.setDescription("Financial movement");
        mov.setInvolved("Involved in transaction");
        mov.setType("REC");
        mov.setTransactionDate("01/01/2023");
        mov.setPaymentDate("15/01/2023");
        mov.setValue(100f);
        mov.setStatus(true);

        given()
                .header("Authorization", "JWT " + TOKEN)
                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .log().all()
                .statusCode(200)
        ;
    }*/

    @Test
    public void whenInsertAMovementPT_thenOperationOk() {
        MovementPT mov = new MovementPT();
        mov.setConta_id(1562804);
        //mov.setUsuario_id();
        mov.setDescricao("Financial movement");
        mov.setEnvolvido("Involved in transaction");
        mov.setTipo("REC");
        mov.setData_transacao("01/01/2020");
        mov.setData_pagamento("15/01/2020");
        mov.setValor(100f);
        mov.setStatus(true);

        given()
                .header("Authorization", "JWT " + TOKEN)
                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .log().all()
                .statusCode(201)
        ;
    }

    @Test
    public void whenInsertAMovementPT2WithoutDescription_thenOperationNotOk() {
        MovementPT mov = createMovementValida();
        mov.setDescricao(null);//testing sending without this tag


        given()
                .header("Authorization", "JWT " + TOKEN)
                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .log().all()
                .statusCode(400)
                .body("msg", hasItem("Descrição é obrigatório"))
        ;
    }

    @Test
    public void whenInsertAMovementPTWithoutTransactionDate_thenOperationNotOk() {
        MovementPT mov = createMovementValida();
        mov.setData_transacao(null);


        given()
                .header("Authorization", "JWT " + TOKEN)
                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .log().all()
                .statusCode(400)
                .body("msg", hasItem("Data da Movimentação é obrigatório"))
        ;
    }

    @Test
    public void whenInsertAMovementPTWithTransactionFutureDate_thenOperationNotOk() {

        MovementPT mov = createMovementValida();
        mov.setData_transacao("19/11/2040");

        given()
                .header("Authorization", "JWT " + TOKEN)
                .body(mov)
                .when()
                .post("/transacoes")
                .then()
                .log().all()
                .statusCode(400)
                .body("$", hasSize(1))
                .body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
        ;
    }

    @Test
    public void whenAccountHasMovement_thenCanNotDeleteAccount() {
        given()
                .header("Authorization", "JWT " + TOKEN)
                .when()
                .delete("/contas/1562804")
                .then()
                .statusCode(500)
                .body("constraint", is("transacoes_conta_id_foreign"))
        ;
    }

    @Test
    public void whenAccountHasBalance_thenCheckTheBalance() {
        given().header("Authorization", "JWT " + TOKEN)
                .when()
                .get("/saldo")
                .then()
                .statusCode(200)
                .body("find{it.conta_id == 1562804}.saldo", is("200.00"))
        ;
    }

    // delete financial movement id = 1472453
    @Test
    public void whenDeleteAccount_thenAccountIsDeleted() {

        ACCOUNT_ID = createUserAccount();

        given()
                .header("Authorization", "JWT " + TOKEN)
                .pathParam("id", ACCOUNT_ID)
                .when()
                .delete("/contas/{id}")
                .then()
                .statusCode(204)
        ;

    }


}

//package requests;
//
//import io.restassured.response.ValidatableResponse;
//import io.restassured.specification.RequestSpecification;
//import io.restassured.specification.ResponseSpecification;
//import models.ChangeUserNameRequest;
//
//import static io.restassured.RestAssured.given;
//
//public class ChangeUserNameRequester extends Request<ChangeUserNameRequest> {
//
//    public ChangeUserNameRequester(RequestSpecification requestSpecification,
//                                   ResponseSpecification responseSpecification) {
//
//        super(requestSpecification, responseSpecification);
//    }
//
//    @Override
//    public ValidatableResponse post(ChangeUserNameRequest model) {
//        return given()
//                .spec(requestSpecification)
//                .body(model)
//                .put("/api/v1/customer/profile")
//                .then()
//                .assertThat()
//                .spec(responseSpecification);
//    }
//}

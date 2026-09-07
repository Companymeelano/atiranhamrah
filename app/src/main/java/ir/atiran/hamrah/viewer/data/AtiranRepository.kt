package ir.atiran.hamrah.viewer.data

/**
 * Typed access to every read-oriented endpoint of the Atiran service.
 * Endpoint paths / body styles come from docs/api-reference.json
 * (extracted from AtiranLocalServices.dll metadata).
 */
class AtiranRepository(private val client: AtiranClient) {

    private val token = client::freshToken

    // ------------------------------------------------------------ auth
    suspend fun login(login: Login): LoginResult? {
        val t = token()
        val body = client.encode(LoginReq.serializer(), LoginReq(login, t))
        return client.callSingle("Post/Login", body, LoginResult.serializer())
    }

    suspend fun loginVisitor(vl: VisitorLogin): VisitorLoginResult? {
        val t = token()
        val body = client.encode(VisitorLoginReq.serializer(), VisitorLoginReq(vl, t))
        return client.callSingle("POST/LoginVisitor", body, VisitorLoginResult.serializer())
    }

    suspend fun getCustomerByLogin(cl: CustomerLogin): List<Customer> {
        val t = token()
        val body = client.encode(CustomerLoginReq.serializer(), CustomerLoginReq(cl, t))
        return client.callList("Post/GetCustomerByLogin", body, Customer.serializer())
    }

    // ------------------------------------------------------------ connection test
    suspend fun companyInfo(): DTOCompany? {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callSingle("Get/CompanyInfo", b, DTOCompany.serializer())
    }

    suspend fun checkSetInfo(setInfo: SetInfo): String? {
        val b = client.encode(SetInfoReq.serializer(), SetInfoReq(setInfo, token()))
        return client.callString("Get/CheckSetInfo", b)
    }

    suspend fun periods(): List<String> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callStringList("Get/GetPeriods", b)
    }

    // ------------------------------------------------------------ customers
    suspend fun customers(start: Int, fetch: Int): List<Customer> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/Customers/$start/$fetch", b, Customer.serializer())
    }

    suspend fun filteredCustomers(setInfo: SetInfo, start: Int, fetch: Int): List<Customer> {
        val b = client.encode(FilteredReq.serializer(), FilteredReq(setInfo, token()))
        return client.callList("Get/FilteredCustomers/$start/$fetch", b, Customer.serializer())
    }

    suspend fun customerByShMo(shMo: String): List<Customer> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/CustomerByShMo/${AtiranClient.enc(shMo)}", b, Customer.serializer())
    }

    suspend fun custGroups(): List<CustGroup> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/CustGroups", b, CustGroup.serializer())
    }

    // ------------------------------------------------------------ products
    suspend fun kalas(start: Int, fetch: Int): List<Kala> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/Kalas/$start/$fetch", b, Kala.serializer())
    }

    suspend fun kalaByShKa(shKa: Long): List<Kala> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/KalaByShKa/${AtiranClient.enc(shKa.toString())}", b, Kala.serializer())
    }

    suspend fun kalasByGroup(groupId: String): List<Kala> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/KalasByGroupId/${AtiranClient.enc(groupId)}", b, Kala.serializer())
    }

    suspend fun foroshPrices(start: Int, fetch: Int): List<ForoshPrice> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/ForoshPrices/$start/$fetch", b, ForoshPrice.serializer())
    }

    suspend fun kaGroups(): List<KaGroup> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/KaGroups", b, KaGroup.serializer())
    }

    // ------------------------------------------------------------ inventory
    suspend fun anbars(): List<Anbar> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/Anbars", b, Anbar.serializer())
    }

    suspend fun kalaAnbs(start: Int, fetch: Int): List<KalaAnb> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/KalaAnbs/$start/$fetch", b, KalaAnb.serializer())
    }

    suspend fun kalaAnbByShKa(shKa: Long): List<KalaAnb> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/KalaAnbByShKa/${AtiranClient.enc(shKa.toString())}", b, KalaAnb.serializer())
    }

    suspend fun inventoryAnbars(start: Int, fetch: Int): List<VWInventoryAnbars> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/InventoryAnbars/$start/$fetch", b, VWInventoryAnbars.serializer())
    }

    // ------------------------------------------------------------ factors
    suspend fun customerFactors(shMo: String): List<SailFactor> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/CustomerFactors/${AtiranClient.enc(shMo)}", b, SailFactor.serializer())
    }

    suspend fun notPaidFactors(shMo: String): List<SailFactor> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/NotPaidFactors/${AtiranClient.enc(shMo)}", b, SailFactor.serializer())
    }

    suspend fun saleLine(sysid: Long): SaleLine? {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callSingle("Get/SaleLine/${AtiranClient.enc(sysid.toString())}", b, SaleLine.serializer())
    }

    // ------------------------------------------------------------ checks
    suspend fun checks(start: Int, fetch: Int): List<Check> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/Checks/$start/$fetch", b, Check.serializer())
    }

    suspend fun allChecks(): List<Check> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/Checks", b, Check.serializer())
    }

    suspend fun customerChecks(shMo: String): List<Check> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/CustomerChecks/${AtiranClient.enc(shMo)}", b, Check.serializer())
    }

    // ------------------------------------------------------------ messages
    suspend fun visitorMessages(start: Int, fetch: Int): List<Message> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/VisitorMessages/$start/$fetch", b, Message.serializer())
    }

    suspend fun allVisitorMessages(): List<Message> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/VisitorMessages", b, Message.serializer())
    }

    suspend fun readMessage(id: Long): Message? {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callSingle("Get/ReadMessage/${AtiranClient.enc(id.toString())}", b, Message.serializer())
    }

    suspend fun unreadMessages(visitorId: String): List<Message> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/GetUnreadMessages/${AtiranClient.enc(visitorId)}", b, Message.serializer())
    }

    // ------------------------------------------------------------ visits
    suspend fun visitorMasir(visitorId: String): List<Masir> {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callList("Get/VisitorMasir/${AtiranClient.enc(visitorId)}", b, Masir.serializer())
    }

    // ------------------------------------------------------------ counts
    suspend fun countKa(): Int? {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callCount("Get/CountKa", b)
    }

    suspend fun countMo(): Int? {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callCount("Get/CountMo", b)
    }

    suspend fun maxShMo(): Int? {
        val b = client.encode(SecurityToken.serializer(), token())
        return client.callCount("Get/MaxShMo", b)
    }
}

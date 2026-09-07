package ir.atiran.hamrah.viewer.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * DTOs mirroring the WCF DataContracts of AtiranLocalServices.dll.
 * The service serializes with JavaScriptSerializer, so JSON keys are the
 * exact C# property names (PascalCase / mixed) collected from the DLL metadata.
 * All fields are nullable with defaults so missing keys never break parsing.
 */

@Serializable
data class AtiranResult(
    val Status: Int = 0,
    val Type: Int = 0,
    val Result: String? = null,
)

@Serializable
data class SecurityToken(
    val CPUID: String = "",
    val Key: String = "",
)

// ------------------------------------------------------------------ auth
@Serializable
data class Login(
    val Username: String = "",
    val Password: String = "",
)

@Serializable
data class LoginResult(
    val Role: Int? = null,
)

@Serializable
data class VisitorLogin(
    val Username: String = "",
    val Password: String = "",
    val AppId: String = "",
)

@Serializable
data class CustomerLogin(
    val Username: String = "",
    val Password: String = "",
    val NewPassword: String? = null,
    val Title: String? = null,
    val Path: String? = null,
    val Location: String? = null,
    val Order: Int? = null,
    val Shmo: Int? = null,
    val Moname: String? = null,
    val Address: String? = null,
    val Job: String? = null,
    val Email: String? = null,
    val Tel1: String? = null,
    val Tel2: String? = null,
    val Cell: String? = null,
    val Credit: Double? = null,
    val Man: Double? = null,
)

@Serializable
data class PasswordWrapper(
    val password: String = "",
)

@Serializable
data class VisitorLoginReq(val visitorLogin: VisitorLogin, val token: SecurityToken)

@Serializable
data class VisitorLoginResult(
    val Stamp: String? = null,
    val PishFactorNo: Long? = null,
    val dis: String? = null,
    val VisRdf: Int? = null,
    val VisName: String? = null,
    val SysId: Int? = null,
)

/** Request wrappers: WCF "Wrapped" body style uses the C# parameter names. */
@Serializable
data class LoginReq(val login: Login, val token: SecurityToken)
@Serializable
data class CustomerLoginReq(val customerLogin: CustomerLogin, val token: SecurityToken)
@Serializable
data class TokenReq(val token: SecurityToken)
@Serializable
data class SetInfoReq(val setInfo: SetInfo, val token: SecurityToken)
@Serializable
data class FilteredReq(val setInfo: SetInfo, val token: SecurityToken)
@Serializable
data class IdTokenReq(val id: String, val token: SecurityToken)
@Serializable
data class ShMoTokenReq(val shMo: String, val token: SecurityToken)
@Serializable
data class VisitorIdTokenReq(val visitorId: String, val token: SecurityToken)
@Serializable
data class ShKaTokenReq(val shKa: String, val token: SecurityToken)
@Serializable
data class GroupIdTokenReq(val groupId: String, val token: SecurityToken)

// ------------------------------------------------------------------ company
@Serializable
data class DTOCompany(
    val Name: String? = null,
    val Address: String? = null,
    val Tel1: String? = null,
    val Tel2: String? = null,
    val Fax: String? = null,
    val CEgh: String? = null,
    val CMeli: String? = null,
    val CPos: String? = null,
    val VisName: String? = null,
    val VisCell: String? = null,
    val Lines: List<DTOLine>? = null,
    val Visitors: List<Visitor>? = null,
    val Anbars: List<Anbar>? = null,
    val Logo: List<Int>? = null,
    val Banks: List<Bank>? = null,
    val BankNames: List<BankName>? = null,
    val PaymentTimes: List<PaymentTime>? = null,
)

@Serializable
data class DTOLine(
    val LineID: Int? = null,
    val Name: String? = null,
    val Address: String? = null,
    val Tel1: String? = null,
    val Tel2: String? = null,
    val Fax: String? = null,
    val ShSabt: String? = null,
    val CPos: String? = null,
    val CEgh: String? = null,
    val kind_f: Int? = null,
    val Eteb: Double? = null,
)

@Serializable
data class Visitor(
    val VisName: String? = null,
    val VisCell: String? = null,
    val VisRdf: Int? = null,
    val SysId: Int? = null,
)

@Serializable
data class Anbar(
    val RdfAnbar: Int? = null,
    val SysId: Int? = null,
    val Name: String? = null,
)

@Serializable
data class Bank(
    val Rdf: Int? = null,
    val Name: String? = null,
)

@Serializable
data class BankName(
    val Rdf: Int? = null,
    val Name: String? = null,
)

@Serializable
data class PaymentTime(
    val Rdf: Int? = null,
    val Days: Int? = null,
    val Name: String? = null,
)

@Serializable
data class SaleLine(
    val SysID: Int? = null,
    val Name: String? = null,
    val Address: String? = null,
    val Tel1: String? = null,
    val Tel2: String? = null,
    val Fax: String? = null,
    val CEgh: String? = null,
    val CPos: String? = null,
)

// ------------------------------------------------------------------ customers
@Serializable
data class Customer(
    val Shmo: Int? = null,
    val Moname: String? = null,
    val VisRdf: Int? = null,
    val Masir: Int? = null,
    val Sharh: String? = null,
    val Address: String? = null,
    val Job: String? = null,
    val Email: String? = null,
    val Tel1: String? = null,
    val Tel2: String? = null,
    val Cell: String? = null,
    val Credit: Double? = null,
    val Man: Double? = null,
    val Active: Boolean? = null,
    val AccessLine: List<Int>? = null,
    val Code: String? = null,
    val Longitude: Double? = null,
    val Latitude: Double? = null,
    val Special: Boolean? = null,
    val BlackList: Boolean? = null,
    val GroupRdf: Int? = null,
    val ChekBargashti: Boolean? = null,
    val DateMaxSale: String? = null,
    val RegionID: Int? = null,
    val CityID: Int? = null,
    val ProvinceID: Int? = null,
)

@Serializable
data class CustGroup(
    val GroupRdf: Int? = null,
    val GroupName: String? = null,
    val PriceNo: Int? = null,
)

// ------------------------------------------------------------------ products
@Serializable
data class Kala(
    val Shka: Long? = null,
    val NaKa: String? = null,
    val VahSanj: String? = null,
    val MohVah: Long? = null,
    val Active: Boolean? = null,
    val PromotionText: String? = null,
    val MaxTaf: Double? = null,
    val ExpirationDate: String? = null,
    val GroupRdf: Int? = null,
    val FinalSalePrice: Double? = null,
    val Coka: String? = null,
    val Bastebandi: String? = null,
    val PTax: Double? = null,
    val PAvarez: Double? = null,
    val AccessLine: List<Int>? = null,
    val ted_f_ja: Int? = null,
    val ted_ja: Int? = null,
    val shka_ja: Long? = null,
    val reducePrizeFromSales: Boolean? = null,
    val prize_table: String? = null,
    val prizePercents: String? = null,
)

@Serializable
data class KalaAnb(
    val ShKa: Long? = null,
    val RdfAnb: Int? = null,
    val Moj: Double? = null,
    val AnbName: String? = null,
    val MohVah: Long? = null,
)

@Serializable
data class KaGroup(
    val GroupRdf: Int? = null,
    val GroupName: String? = null,
    val ParentGroupRdf: Int? = null,
    val prizePercentGroup: String? = null,
    val prize_Table_Group: String? = null,
)

@Serializable
data class ForoshPrice(
    val ShKa: Long? = null,
    val FP1: Double? = null, val MP1: Int? = null, val VP1: Double? = null,
    val FP2: Double? = null, val MP2: Int? = null, val VP2: Double? = null,
    val FP3: Double? = null, val MP3: Int? = null, val VP3: Double? = null,
    val FP4: Double? = null, val MP4: Int? = null, val VP4: Double? = null,
    val FP5: Double? = null, val MP5: Int? = null, val VP5: Double? = null,
)

@Serializable
data class VWInventoryAnbars(
    val shka: Long? = null,
    val rdf_anbars: Int? = null,
    val name: String? = null,
    val tedbastebandi: Int? = null,
    val mojkavah: Double? = null,
    val mojkajoz: Double? = null,
    val MojodiPish_vah: Double? = null,
    val MojodiPish_joz: Double? = null,
    val RdfAnbar: Int? = null,
    val AnbName: String? = null,
    val Moj: Double? = null,
    val MohVah: Long? = null,
)

// ------------------------------------------------------------------ factors
@Serializable
data class SailFactor(
    val Id: Int? = null,
    val ShFact: Long? = null,
    val Date: String? = null,
    val ShMo: Int? = null,
    val TDate: String? = null,
    val AllFel: Double? = null,
    val MabDaryaftFactor: Double? = null,
    val Tdf: Double? = null,
)

@Serializable
data class FactorDetail(
    val FactNo: Long? = null,
    val Shmo: Int? = null,
    val NTakhfif: Double? = null, val NBarbari: Double? = null, val NTax: Double? = null,
    val NAvarez: Double? = null, val NJamekol: Double? = null,
    val BTakhfif: Double? = null, val BjAM: Double? = null, val BBarbari: Double? = null,
    val BTax: Double? = null, val BAvarez: Double? = null, val BJamekol: Double? = null,
    val KhTakhfif: Double? = null, val KhBarbari: Double? = null, val KhTax: Double? = null,
    val KhAvarez: Double? = null, val KhJamekol: Double? = null,
    val State: String? = null,
    val Date: String? = null,
    val description: String? = null,
    val JOZPRICE: Double? = null,
    val mohvah: Long? = null,
    val KHtedjoz: Int? = null,
    val KHtedvah: Double? = null,
    val pertaf: Double? = null,
    val Shka: Long? = null,
)

@Serializable
data class PishFactor(
    val shfacfo: Long? = null,
    val price: Double? = null,
    val status: Int? = null,
    val description: String? = null,
    val date: String? = null,
    val visitor: String? = null,
    val customer: String? = null,
)

// ------------------------------------------------------------------ checks
@Serializable
data class Check(
    val Id: Long? = null,
    val Date: String? = null,
    val SarDate: String? = null,
    val getchkshhes: String? = null,
    val Bank: String? = null,
    val Shobe: String? = null,
    val Serial: String? = null,
    val Mablagh: Double? = null,
    val shmo: Long? = null,
    val CheckDescription: String? = null,
    val Status: Int? = null,
)

// ------------------------------------------------------------------ messages
@Serializable
data class Message(
    val Id: Long? = null,
    val ReadTimeDate: String? = null,
    val ReadTime: String? = null,
    val ReadDate: String? = null,
    val CreateDate: String? = null,
    val CreateTimeDate: String? = null,
    val CreateTime: String? = null,
    val Title: String? = null,
    val isImportant: Int? = null,
    val VisitorId: Long? = null,
    val Description: String? = null,
    val Status: Int? = null,
)

@Serializable
data class ListMessage(
    val rowID: Long? = null,
    val sendDate: String? = null,
    val message: String? = null,
    val title: String? = null,
    val forcedUpdate: Boolean? = null,
)

@Serializable
data class MessageFilter(
    val StartDate: String? = null,
    val EndDate: String? = null,
    val VisitorID: Int? = null,
)

@Serializable
data class MessageFilterReq(val messageFilter: MessageFilter, val token: SecurityToken)

// ------------------------------------------------------------------ visits / masir
@Serializable
data class Visit(
    val VisitID: Long? = null,
    val Sign: String? = null,
    val VisRdf: Int? = null,
    val Shmo: Int? = null,
    val Duration: Int? = null,
    val Created: Long? = null,
    val Sent: Long? = null,
    val Description: String? = null,
    val SentLng: Float? = null,
    val SentLat: Float? = null,
    val SaveLat: Float? = null,
    val SaveLng: Float? = null,
    val SysID: Int? = null,
    val RdfAnbar: Int? = null,
    val IsKalaGostaran: Boolean? = null,
    val DateCreated: String? = null,
    val DateSent: String? = null,
    val TimeCreated: String? = null,
    val TimeSent: String? = null,
)

@Serializable
data class Order(
    val RawPrice: Double? = null,
    val DiscountPrice: Double? = null,
    val TotalPrice: Double? = null,
    val Created: Long? = null,
    val CreatedDate: String? = null,
    val Description: String? = null,
    val Stamp: String? = null,
    val Status: Int? = null,
    val MpKol: Int? = null,
    val MpIsAuto: Boolean? = null,
)

@Serializable
data class OrderDetail(
    val ProductID: Int? = null,
    val RawPrice: Double? = null,
    val DiscountPrice: Double? = null,
    val TotalPrice: Double? = null,
    val UnitCount: Double? = null,
    val DetailCount: Int? = null,
    val RdfAnbar: Int? = null,
    val JozPrice: Double? = null,
    val discountPercent: Double? = null,
    val ProductName: String? = null,
    val Mp: Int? = null,
)

@Serializable
data class Masir(
    val ID: Int? = null,
    val shomareMasir: Int? = null,
    val visRdf: Int? = null,
    val Date: String? = null,
)

// ------------------------------------------------------------------ misc
@Serializable
data class SetInfo(
    val shmo: String? = null,
    val password: String? = null,
    val _ownerNum: String? = null,
    val _InventoryNum: String? = null,
    val _AtiranNum: String? = null,
    val _carrierNum: String? = null,
    val isRoozMasir: Boolean? = null,
    val isRoozMasir_new: Boolean? = null,
    val roozMasir: Boolean? = null,
    val _ActiveLine: String? = null,
    val _serverAdd: String? = null,
    val defaultPriceGrp: Int? = null,
    val PriceGrpType: Int? = null,
    val VisitRangeLimit: Int? = null,
    val ActiveRefund: Long? = null,
    val mojoodiType: Int? = null,
    val PrintCount: Int? = null,
    val timeF: Int? = null,
    val displacement: Int? = null,
    val ActsLimit: Int? = null,
    val MaxAllowedSyncDays: Int? = null,
    val NewVis4NoLoc: Boolean? = null,
    val VisitHasLocation: Boolean? = null,
    val discountApply: Boolean? = null,
    val jozChange: Boolean? = null,
    val sendLoc: Boolean? = null,
    val AccessReport: Boolean? = null,
    val VisitorSeeIP: Boolean? = null,
    val SelectByMasir: Boolean? = null,
    val hasAccessPishDaryaft: Boolean? = null,
    val hasAccessPishFactor: Boolean? = null,
    val hasAccessRahyab: Boolean? = null,
    val serverNamesList: String? = null,
    val serversList: String? = null,
    val selectivePrice: Boolean? = null,
    val selectiveMp: Boolean? = null,
    val VisitorID: Int? = null,
    val CheckingBouncedCheck: Boolean? = null,
    val InventoryCheck: Int? = null,
    val AutoPrize: Boolean? = null,
    val PercentGroupPriority: Boolean? = null,
    val PrizeGroup_Vahed_Joz: Int? = null,
    val IsTaxActive: Boolean? = null,
)

@Serializable
data class TabletCustomer(
    val vis_rdf: Int? = null,
    val shmo: Int? = null,
    val name: String? = null,
    val melli_code: String? = null,
    val tell1: String? = null,
    val tell2: String? = null,
    val cell: String? = null,
    val address: String? = null,
    val sharh: String? = null,
    val owners_count: Int? = null,
    val metraj_shop: Double? = null,
    val metraj_yakhchal: Double? = null,
    val yakhchal_count: Int? = null,
    val tablo: String? = null,
    val sabeghe: Int? = null,
    val vis_name: String? = null,
    val estijari: Boolean? = null,
    val create_date: String? = null,
    val birth_date: String? = null,
    val id: Int? = null,
    val Lat: Double? = null,
    val Lng: Double? = null,
)

@Serializable
data class LocationVisitor(
    val nameVis: String? = null,
    val VisRdf: Int? = null,
    val date: String? = null,
    val time: String? = null,
    val Longitude: Double? = null,
    val Latitude: Double? = null,
)

@Serializable
data class IntervalList(
    val Rdf: Int? = null,
    val VisName: String? = null,
    val PriceTarget: Double? = null,
    val CountTarget: Int? = null,
    val IntervalName: String? = null,
    val StartInterval: String? = null,
    val EndInterval: String? = null,
    val UserName: String? = null,
    val RowID: Int? = null,
    val CopyInterval: Int? = null,
    val KaGroupName: String? = null,
    val naka: String? = null,
    val CusGroupName: String? = null,
    val ProvinceName: String? = null,
    val CityName: String? = null,
    val RegionName: String? = null,
    val PathName: String? = null,
    val VisID: Int? = null,
    val CusGroupRdf: Int? = null,
    val KalaGroupRdf: Int? = null,
    val InventoryID: Long? = null,
    val ProvinceID: Int? = null,
    val CityID: Int? = null,
    val RegionID: Int? = null,
    val PathID: Int? = null,
)

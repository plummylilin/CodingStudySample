/*
* 使用salesforce通过REST方式作为webservice，需要以下几点：
* 1.类和方法需要global，方法需要静态
* 2.类需要通过RestResource（UrlMapping='/page/*'）注解声明
* 3.@HttpGet和@HttpDelete不能有形参，可以通过URL?param或者URL/param方式传过来参数
*/
@RestResource(UrlMapping='/PunchCard/*')
global class PunchCardRESTController {
    global static final Integer PAGE_SIZE = 20;
    @HttpGet
    global static List<Punch_Card__c> getPunchCardByIdOrPunchCardList() {
        System.debug('---------getPunchCardByIdOrPunchCardList start-------------');
        RestRequest request = RestContext.request;
        // grab the PunchCardId from the end of the URL
        String currentPage = request.params.get('currentPage') != null ? request.params.get('currentPage') : '0';
        Integer offsetNumber = Integer.valueOf(currentPage) * PAGE_SIZE;
        String punchCardId = request.params.get('punchCardId');  //workbench 的写法： /services/apexrest/PunchCard/getPunchCardByIdOrPunchCardList?punchCardId=a0D2800000aVuau
        system.debug('punchCardId:'+punchCardId);
        String fetchSql;
        if(punchCardId != null) {
            fetchSql = 'SELECT CreatedById, CreatedDate, Category__c, subCategory__c,' +
            ' Date__c, Hours__c, Notes__c, LastModifiedById, LastModifiedDate, OwnerId, Id' +
            ' FROM Punch_Card__c where Id = :punchCardId';
        } else {
            fetchSql = 'SELECT CreatedById, CreatedDate, Category__c, subCategory__c,' +
            ' Date__c, Hours__c, Notes__c, LastModifiedById, LastModifiedDate, OwnerId, Id' +
            ' FROM Punch_Card__c limit :PAGE_SIZE offset :offsetNumber';
        }
        List<Punch_Card__c> punchCardList = Database.query(fetchSql);
        System.debug('---------getPunchCardByIdOrPunchCardList end-------------');
        return punchCardList;
    }
    
    
    @HttpPost
    global static Id insertPunchCard(String category,String subCategory,Decimal hours, String isUseful) {
        System.debug('---------insertPunchCard start-------------');
        Punch_Card__c punchCard = new Punch_Card__c();
        if(category != null && subCategory != null && hours != null && isUseful != null) {
            punchCard.Category__c = category;
            punchCard.subCategory__c = subCategory;
            punchCard.Hours__c = hours;
            punchCard.Is_This_Resource_useful__c = isUseful;
            punchCard.Notes__c = 'Data is from Java:HttpPost';
        }

        insert punchCard;
        System.debug('---------insertPunchCard end-------------');
        return punchCard.Id;
    }
    
    @HttpDelete
    global static void deletePunchCard() {
        System.debug('---------deletePunchCard start-------------');
        RestRequest request = RestContext.request;
        String punchCardId = request.requestURI.substring(
            request.requestURI.lastIndexOf('/')+1);
        Punch_Card__c needDeletePunchCard = [select Id from Punch_Card__c where Id = :punchCardId];
        if(needDeletePunchCard != null) {
            delete needDeletePunchCard;
        }
        System.debug('---------deletePunchCard end-------------');
    }
    
    @HttpPut
    global static ID upsertPunchCard(String id,String category,String subCategory,Decimal hours,String isUseful) {
        System.debug('---------upsertPunchCard start-------------');
        Punch_Card__c punchCard = new Punch_Card__c();
        punchCard.Id = id;
        if(category != null && subCategory != null) {
            punchCard.Category__c = category;
            punchCard.subCategory__c = subCategory;
        }
        if(hours != null) {
            punchCard.Hours__c = hours;
        }
        if(isUseful != null) {
            punchCard.Is_This_Resource_useful__c = isUseful;
        }

        upsert punchCard;
        System.debug('---------upsertPunchCard end-------------');
        return punchCard.Id;
    }

    @HttpPatch
    global static ID updatePunchCard() {
        System.debug('---------updatePunchCard start-------------');
        RestRequest request = RestContext.request;
        String punchCardId = request.requestURI.substring(
            request.requestURI.lastIndexOf('/')+1);
        Punch_Card__c punchCard = [select Id from Punch_Card__c where Id= :punchCardId];
        // Deserialize the JSON string into name-value pairs
        Map<String, Object> params = (Map<String, Object>)JSON.deserializeUntyped(request.requestbody.tostring());
        // Iterate through each parameter field and value
        punchCard.Notes__c = String.valueOf(params.get('Notes__c'));
        update punchCard;
        System.debug('---------updatePunchCard end-------------');
        return punchCard.Id;
    } 
    
    
    
}

import { util } from '@aws-appsync/utils';
/**
 * Sends a request to the attached data source
 * @param {import('@aws-appsync/utils').Context} ctx the context
 * @returns {*} the request
 */
export function request(ctx) {
    const { userId, payLoad } = ctx.args || {};
    
    if (!userId || !payLoad) {
        return util.error("Missing required arguments: userId and payLoad", "BadRequest");
    }

    return {
        operation: 'PutItem',
        key: {
            id: util.dynamodb.toDynamoDB(util.autoId()),  
        },
        attributeValues: util.dynamodb.toMapValues({
            userId: userId,
            createdAt: util.time.nowISO8601(), 
            payLoad: payLoad
        })
    };
}

/**
 * Returns the resolver result.
 * @param {import('@aws-appsync/utils').Context} ctx the context
 * @returns {*} the result
 */
export function response(ctx) {
    if (ctx.error) {
        return util.error(ctx.error.message, ctx.error.type);
    }
    
    return ctx.result;
}

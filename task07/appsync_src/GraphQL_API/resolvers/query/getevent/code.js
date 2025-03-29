import { util } from '@aws-appsync/utils';
/**
 * Sends a request to the attached data source
 * @param {import('@aws-appsync/utils').Context} ctx the context
 * @returns {*} the request
 */
export function request(ctx) {
    const { id } = ctx.args || {};
    
    if (!id) {
        return util.error("Missing required argument: id", "BadRequest");
    }

    return {
        operation: 'GetItem',
        key: {
            id: util.dynamodb.toDynamoDB(id)
        }
    };
}

/**
 * Returns the fetched event from DynamoDB.
 * @param {import('@aws-appsync/utils').Context} ctx the context
 * @returns {*} the result
 */
export function response(ctx) {
    if (ctx.error) {
        return util.error(ctx.error.message, ctx.error.type);
    }
    
    if (!ctx.result) {
        return util.error("Event not found.", "NotFoundError");
    }

    return ctx.result;
}

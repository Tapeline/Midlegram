from midlegram.common import dto


@dto
class Pagination:
    limit: int
    offset: int = 0

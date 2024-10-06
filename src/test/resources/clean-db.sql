begin transaction;

truncate public.comment,
    public.fav,
    public.adv_pics,
    public.adv,
    public.refresh_token,
    public.users;

commit;
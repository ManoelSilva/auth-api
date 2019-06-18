# Auth-API Spring-boot e Spring-Security

API RESTful de criação de usuários e login.


Formato das mensagens de erro:

```json
    {"mensagem": "mensagem de erro"}
```

## Cadastro

* Endpoint que recebe um usuário com os campos "nome", "email", "senha", mais uma lista de objetos "telefone", seguindo o formato:

```json
    {
        "name": "Manoel Silva",
        "email": "manoelps.ti@gmail.com",
        "password": "developer",
        "phones": [
            {
                "number": "986655473",
                "ddd": "85"
            }
        ]
    }
```

* Em caso de sucesso:
    * `id`: id do usuário
    * `created`: data da criação do usuário
    * `modified`: data da última atualização do usuário
    * `last_login`: data do último login
    * `token`: token de acesso da API (JWT)

* Caso o e-mail já exista, retorna erro com a mensagem "E-mail já existente".
* O token é persistido junto com o usuário.

## Login

* Este endpoint recebe um objeto com e-mail e senha.
* Caso o e-mail e a senha correspondam a um usuário existente, retorna igual ao endpoint de Criação.
* Caso o e-mail não exista, retorna código 401 e a mensagem "Usuário e/ou senha inválidos".
* Caso o e-mail exista mas a senha esteja inválida, retorna código 401 e a mensagem "Usuário e/ou senha inválidos".

## Perfil do Usuário
* Caso o token não exista, 401 e a mensagem "Não autorizado".
* Caso o token exista, busca o usuário pelo `id` passado no path e compara se o token no modelo é igual ao token passado no header.
* Caso não seja o mesmo token, retorna 401 e mensagem "Não autorizado"
* Caso seja o mesmo token, varifica se o último login foi a MENOS que 30 minutos atrás. Caso não seja a MENOS que 30 minutos atrás, retorna 401 com a mensagem "Sessão inválida".
* Caso tudo esteja ok, retorna o usuário no mesmo formato do retorno do Login.

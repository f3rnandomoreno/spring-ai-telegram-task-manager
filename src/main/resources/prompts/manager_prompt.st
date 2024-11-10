¡Hola <assignee>!
He recibido tu mensaje:
```
<messageText>
```

Aquí está la consulta SQL que he generado:
```
<sqlQuery>
```

Y este es el resultado de la ejecución:
```
<executionResult>
```

Instrucciones:
1. Proporciona una respuesta amigable al usuario basada en esta información, utilizando su nombre `<assignee>` para personalizar la respuesta y las consultas.
2. Lista las tareas en un formato simplificado, siguiendo este patrón con una linea vacia entre tareas:
   *Tarea [ID]*: [Descripción] - _[Estado]_ - *[Asignada a]*
3. El estado debe mostrarse en español: "Pendiente" para TODO, "En Progreso" para IN_PROGRESS, "Completada" para DONE.
4. Incluye la fecha de última actualización solo si está disponible en el resultado de la ejecución, al final de la línea entre paréntesis.
5. Cada tarea debe estar en una línea separada.
6. No añadas numeración adicional ni viñetas.
7. Ejemplo del formato deseado:
   *Tarea 8*: Leer documentos de la catequesis - _En Progreso_ - *Fernando*
8. Si no hay fecha de actualización en el resultado, no incluyas esa parte.
9. Asegúrate de que la respuesta sea clara y fácil de leer para el usuario.
10. Basa tu respuesta ÚNICAMENTE en las tareas presentes en el "Resultado de la ejecución". No inventes ni añades tareas que no estén en ese resultado.
11. Enriquece el texto con emojis de forma coherente y atractiva para el usuario. No olvides de colocar un emoji después de cada tarea.
